package com.thelaziest.VideoSummarizer.video;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Paragraph {
    private String text;

    private int start;
    private int end;
    private float confidence;
    private ArrayList<Sentence> sentences = new ArrayList<>();

    @JsonIgnore
    private ArrayList<Word> words;

    public void addSentence(Sentence sentence) {
        sentences.add(sentence);
    }
}
