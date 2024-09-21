package com.thelaziest.VideoSummarizer.video;

import java.util.List;

public class TranscriptUtils {
    public static void buildParagraphFromSentence(List<Paragraph> paragraphs, List<Sentence> sentences){
        int i = 0;
        for(Paragraph p : paragraphs){
            int start = p.getStart();
            int end = p.getEnd();

            for(; i < sentences.size(); i++){
                // because sentences is sort increasing, so just add them to paragraph, it is increasing as well.
                if(sentences.get(i).getStart() >= start && sentences.get(i).getEnd() <= end){
                    p.addSentence(sentences.get(i));
                } else {
                    break;
                }
            }
        }
    }
}
