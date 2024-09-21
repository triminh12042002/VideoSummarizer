package com.thelaziest.VideoSummarizer.video;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Sentence {
    private String text;
    private int start;
    private int end;
    private float confidence;
    @JsonIgnore
    ArrayList<Word> words;
}
