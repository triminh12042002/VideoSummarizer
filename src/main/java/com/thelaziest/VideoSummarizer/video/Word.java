package com.thelaziest.VideoSummarizer.video;

import lombok.Data;

@Data
public class Word {
    private String text;
    private int start;
    private int end;
    private float confidence;
}
