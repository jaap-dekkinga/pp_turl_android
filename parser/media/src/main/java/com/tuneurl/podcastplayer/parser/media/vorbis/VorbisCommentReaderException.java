package com.tuneurl.podcastplayer.parser.media.vorbis;

public class VorbisCommentReaderException extends Exception {
    private static final long serialVersionUID = 1L;

    public VorbisCommentReaderException(String message) {
        super(message);
    }

    public VorbisCommentReaderException(Throwable message) {
        super(message);
    }
}
