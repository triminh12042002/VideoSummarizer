package com.thelaziest.VideoSummarizer;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.requests.TranscriptParams;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.thelaziest.VideoSummarizer.video.Paragraph;
import com.thelaziest.VideoSummarizer.video.Sentence;
import com.thelaziest.VideoSummarizer.video.VideoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
//import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.thelaziest.VideoSummarizer.video.TranscriptUtils.buildParagraphFromSentence;
import static com.thelaziest.VideoSummarizer.video.VideoUtils.*;

@SpringBootApplication
public class VideoSummarizerApplication {

	public static void main(String[] args) {

//		ApplicationContext context = SpringApplication.run(VideoSummarizerApplication.class, args);
		String videoPath = "src/main/resources/video/input/Why Messi Is better than ronaldo _720pFH.mp4";
//		String audioPath = convertVideoToAudio(videoPath);

//		VideoUtils videoUtils = context.getBean(VideoUtils.class);
//		String audioPath ="src/main/resources/video/audio/Why Messi Is better than ronaldo _720pFH.mp3";
//		videoUtils.loadTranscript(audioPath);
//
		String paragraphsPath = "paragraphsAudioTranscript.json";
		List<Paragraph> paragraphs = ConvertJsonToObject(paragraphsPath, Paragraph.class);

		String sentencesPath = "sentencesAudioTranscript.json";
		List<Sentence> sentences = ConvertJsonToObject(sentencesPath, Sentence.class);

		buildParagraphFromSentence(paragraphs, sentences);

//		visualizeParagraph(paragraphs);

		String outputPath = "src/main/resources/video/output/video2/output.mp4";
		cutAndMergeVideoBySentences(videoPath, outputPath, paragraphs);
	}

	public static void visualizeParagraph(List<Paragraph> paragraphs){
		for(int i = 0; i < paragraphs.size(); i++) {
			List<Sentence> pSentences = paragraphs.get(i).getSentences();
			System.out.println("paragraph: " + i);
			for(int j = 0; j < pSentences.size(); j++) {
				Sentence sentence = pSentences.get(j);
				System.out.println(j + ": " + convertMillisecondsToStringTimeFormat(sentence.getStart()) +  " " + sentence.getStart()  +  " "+ sentence.getText());
			}
		}
	}

	public static String convertMillisecondsToStringTimeFormat(int mills){
		String time = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(mills + 16 * 60 * 60 * 1000));
		return time;
	}



	public static <T> List<T> ConvertJsonToObject(String filePath, Class<T> convertType) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();

			CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, convertType);

			// read JSON from a file and convert it to Java object
			List<T> paragraphs = objectMapper.readValue(new File(filePath), listType);

//			for(int i = 0; i < paragraphs.size(); i++) {
//				System.out.println(i + ":" + paragraphs.get(i).getText());
//			}

			return paragraphs;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
