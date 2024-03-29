package com.dekidea.hearact.core.storage;

import com.dekidea.hearact.core.service.download.DownloadService;
import com.dekidea.hearact.model.playback.RemoteMedia;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.dekidea.hearact.model.feed.FeedComponent;
import com.dekidea.hearact.model.feed.FeedItem;
import com.dekidea.hearact.model.feed.FeedMedia;
import com.dekidea.hearact.core.feed.FeedMother;
import com.dekidea.hearact.core.preferences.UserPreferences.EnqueueLocation;
import com.dekidea.hearact.model.playback.Playable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.dekidea.hearact.core.preferences.UserPreferences.EnqueueLocation.AFTER_CURRENTLY_PLAYING;
import static com.dekidea.hearact.core.preferences.UserPreferences.EnqueueLocation.BACK;
import static com.dekidea.hearact.core.preferences.UserPreferences.EnqueueLocation.FRONT;
import static com.dekidea.hearact.core.util.CollectionTestUtil.concat;
import static com.dekidea.hearact.core.util.CollectionTestUtil.list;
import static com.dekidea.hearact.core.util.FeedItemUtil.getIdList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class ItemEnqueuePositionCalculatorTest {

    @RunWith(Parameterized.class)
    public static class BasicTest {
        @Parameters(name = "{index}: case<{0}>, expected:{1}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"case default, i.e., add to the end",
                            concat(QUEUE_DEFAULT_IDS, TFI_ID),
                            BACK, QUEUE_DEFAULT},
                    {"case option enqueue at front",
                            concat(TFI_ID, QUEUE_DEFAULT_IDS),
                            FRONT, QUEUE_DEFAULT},
                    {"case empty queue, option default",
                            list(TFI_ID),
                            BACK, QUEUE_EMPTY},
                    {"case empty queue, option enqueue at front",
                            list(TFI_ID),
                            FRONT, QUEUE_EMPTY},
            });
        }

        @Parameter
        public String message;

        @Parameter(1)
        public List<Long> idsExpected;

        @Parameter(2)
        public EnqueueLocation options;

        @Parameter(3)
        public List<FeedItem> curQueue;

        public static final long TFI_ID = 101;

        /**
         * Add a FeedItem with ID {@link #TFI_ID} with the setup
         */
        @Test
        public void test() {
            ItemEnqueuePositionCalculator calculator = new ItemEnqueuePositionCalculator(options);

            // shallow copy to which the test will add items
            List<FeedItem> queue = new ArrayList<>(curQueue);
            FeedItem tFI = createFeedItem(TFI_ID);
            doAddToQueueAndAssertResult(message,
                    calculator, tFI, queue, getCurrentlyPlaying(),
                    idsExpected);
        }

        Playable getCurrentlyPlaying() {
            return null;
        }
    }

    @RunWith(Parameterized.class)
    public static class AfterCurrentlyPlayingTest extends BasicTest {
        @Parameters(name = "{index}: case<{0}>, expected:{1}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {"case option after currently playing",
                            list(11L, TFI_ID, 12L, 13L, 14L),
                            AFTER_CURRENTLY_PLAYING, QUEUE_DEFAULT, 11L},
                    {"case option after currently playing, currently playing in the middle of the queue",
                            list(11L, 12L, 13L, TFI_ID, 14L),
                            AFTER_CURRENTLY_PLAYING, QUEUE_DEFAULT, 13L},
                    {"case option after currently playing, currently playing is not in queue",
                            concat(TFI_ID, QUEUE_DEFAULT_IDS),
                            AFTER_CURRENTLY_PLAYING, QUEUE_DEFAULT, 99L},
                    {"case option after currently playing, no currentlyPlaying is null",
                            concat(TFI_ID, QUEUE_DEFAULT_IDS),
                            AFTER_CURRENTLY_PLAYING, QUEUE_DEFAULT, ID_CURRENTLY_PLAYING_NULL},
                    {"case option after currently playing, currentlyPlaying is not a feedMedia",
                            concat(TFI_ID, QUEUE_DEFAULT_IDS),
                            AFTER_CURRENTLY_PLAYING, QUEUE_DEFAULT, ID_CURRENTLY_PLAYING_NOT_FEEDMEDIA},
                    {"case empty queue, option after currently playing",
                            list(TFI_ID),
                            AFTER_CURRENTLY_PLAYING, QUEUE_EMPTY, ID_CURRENTLY_PLAYING_NULL},
            });
        }

        @Parameter(4)
        public long idCurrentlyPlaying;

        @Override
        Playable getCurrentlyPlaying() {
            return ItemEnqueuePositionCalculatorTest.getCurrentlyPlaying(idCurrentlyPlaying);
        }

        private static final long ID_CURRENTLY_PLAYING_NULL = -1L;
        private static final long ID_CURRENTLY_PLAYING_NOT_FEEDMEDIA = -9999L;

    }

    @RunWith(Parameterized.class)
    public static class PreserveDownloadOrderTest {
        /**
         * The test covers the use case that when user initiates multiple downloads in succession,
         * resulting in multiple addQueueItem() calls in succession.
         * the items in the queue will be in the same order as the order user taps to download
         */
        @Parameters(name = "{index}: case<{0}>")
        public static Iterable<Object[]> data() {
            // Attempts to make test more readable by showing the expected list of ids
            // (rather than the expected positions)
            return Arrays.asList(new Object[][] {
                    {"download order test, enqueue default",
                            concat(QUEUE_DEFAULT_IDS, 101L),
                            concat(QUEUE_DEFAULT_IDS, list(101L, 102L)),
                            concat(QUEUE_DEFAULT_IDS, list(101L, 102L, 103L)),
                            BACK, QUEUE_DEFAULT, ID_CURRENTLY_PLAYING_NULL},
                    {"download order test, enqueue at front (currently playing has no effect)",
                            concat(101L, QUEUE_DEFAULT_IDS),
                            concat(list(101L, 102L), QUEUE_DEFAULT_IDS),
                            concat(list(101L, 103L, 102L), QUEUE_DEFAULT_IDS),
                            // ^ 103 is put ahead of 102, after 102 failed.
                            // It is a limitation as the logic can't tell 102 download has failed
                            // (as opposed to simply being enqueued)
                            FRONT, QUEUE_DEFAULT, 11L}, // 11 is at the front, currently playing
                    {"download order test, enqueue after currently playing",
                            list(11L, 101L, 12L, 13L, 14L),
                            list(11L, 101L, 102L, 12L, 13L, 14L),
                            list(11L, 101L, 103L, 102L, 12L, 13L, 14L),
                            AFTER_CURRENTLY_PLAYING, QUEUE_DEFAULT, 11L}  // 11 is at the front, currently playing
            });
        }

        @Parameter
        public String message;

        @Parameter(1)
        public List<Long> idsExpectedAfter101;

        @Parameter(2)
        public List<Long> idsExpectedAfter102;

        @Parameter(3)
        public List<Long> idsExpectedAfter103;

        @Parameter(4)
        public EnqueueLocation options;

        @Parameter(5)
        public List<FeedItem> queueInitial;

        @Parameter(6)
        public long idCurrentlyPlaying;

        @Test
        public void testQueueOrderWhenDownloading2Items() {
            ItemEnqueuePositionCalculator calculator = new ItemEnqueuePositionCalculator(options);
            try (MockedStatic<DownloadService> downloadServiceMock = Mockito.mockStatic(DownloadService.class)) {
                List<FeedItem> queue = new ArrayList<>(queueInitial);

                // Test body
                Playable currentlyPlaying = getCurrentlyPlaying(idCurrentlyPlaying);
                // User clicks download on feed item 101
                FeedItem feedItem101 = createFeedItem(101);
                downloadServiceMock.when(() ->
                        DownloadService.isDownloadingFile(feedItem101.getMedia().getDownload_url())).thenReturn(true);
                doAddToQueueAndAssertResult(message + " (1st download)",
                        calculator, feedItem101, queue, currentlyPlaying, idsExpectedAfter101);
                // Then user clicks download on feed item 102
                FeedItem feedItem102 = createFeedItem(102);
                downloadServiceMock.when(() ->
                        DownloadService.isDownloadingFile(feedItem102.getMedia().getDownload_url())).thenReturn(true);
                doAddToQueueAndAssertResult(message + " (2nd download, it should preserve order of download)",
                        calculator, feedItem102, queue, currentlyPlaying, idsExpectedAfter102);
                // simulate download failure case for 102
                downloadServiceMock.when(() ->
                        DownloadService.isDownloadingFile(feedItem102.getMedia().getDownload_url())).thenReturn(false);
                // Then user clicks download on feed item 103
                FeedItem feedItem103 = createFeedItem(103);
                downloadServiceMock.when(() ->
                        DownloadService.isDownloadingFile(feedItem103.getMedia().getDownload_url())).thenReturn(true);
                doAddToQueueAndAssertResult(message
                                + " (3rd download, with 2nd download failed; "
                                + "it should be behind 1st download (unless enqueueLocation is BACK)",
                        calculator, feedItem103, queue, currentlyPlaying, idsExpectedAfter103);
            }
        }
    }

    static void doAddToQueueAndAssertResult(String message,
                                            ItemEnqueuePositionCalculator calculator,
                                            FeedItem itemToAdd,
                                            List<FeedItem> queue,
                                            Playable currentlyPlaying,
                                            List<Long> idsExpected) {
        int posActual = calculator.calcPosition(queue, currentlyPlaying);
        queue.add(posActual, itemToAdd);
        assertEquals(message, idsExpected, getIdList(queue));
    }

    static final List<FeedItem> QUEUE_EMPTY = Collections.unmodifiableList(emptyList());

    static final List<FeedItem> QUEUE_DEFAULT = 
            Collections.unmodifiableList(Arrays.asList(
                    createFeedItem(11), createFeedItem(12), createFeedItem(13), createFeedItem(14)));
    static final List<Long> QUEUE_DEFAULT_IDS =
            QUEUE_DEFAULT.stream().map(FeedComponent::getId).collect(Collectors.toList());


    static Playable getCurrentlyPlaying(long idCurrentlyPlaying) {
        if (ID_CURRENTLY_PLAYING_NOT_FEEDMEDIA == idCurrentlyPlaying) {
            return externalMedia();
        }
        if (ID_CURRENTLY_PLAYING_NULL == idCurrentlyPlaying) {
            return null;
        }
        return createFeedItem(idCurrentlyPlaying).getMedia();
    }

    static Playable externalMedia() {
        return new RemoteMedia(createFeedItem(0));
    }

    static final long ID_CURRENTLY_PLAYING_NULL = -1L;
    static final long ID_CURRENTLY_PLAYING_NOT_FEEDMEDIA = -9999L;


    static FeedItem createFeedItem(long id) {
        FeedItem item = new FeedItem(id, "Item" + id, "ItemId" + id, "url",
                new Date(), FeedItem.PLAYED, FeedMother.anyFeed());
        FeedMedia media = new FeedMedia(item, "http://download.url.net/" + id, 1234567, "audio/mpeg");
        media.setId(item.getId());
        item.setMedia(media);
        return item;
    }

}
