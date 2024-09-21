package com.thelaziest.VideoSummarizer;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.requests.TranscriptParams;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.thelaziest.VideoSummarizer.video.Paragraph;
import com.thelaziest.VideoSummarizer.video.Sentence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
import static com.thelaziest.VideoSummarizer.video.VideoUtils.cutAndMergeVideoBySentences;

@SpringBootApplication
public class VideoSummarizerApplication {

	@Value("assemblyai.api.key")
	private static String apiKey;

	public static void main(String[] args) {

//		SpringApplication.run(VideoSummarizerApplication.class, args);

//		loadTranscript();
		String paragraphsPath = "paragraphsAudioTranscript.json";
		List<Paragraph> paragraphs = ConvertJsonToObject(paragraphsPath, Paragraph.class);

		String sentencesPath = "sentencesAudioTranscript.json";
		List<Sentence> sentences = ConvertJsonToObject(sentencesPath, Sentence.class);

		buildParagraphFromSentence(paragraphs, sentences);

//		visualizeParagraph(paragraphs);

		String inputPath = "src/main/video/SnapSave.io-The Entire History Of Cristiano Ronaldo.mp4";
		String outputPath = "output.mp4";
		cutAndMergeVideoBySentences(inputPath, outputPath, paragraphs);
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
	public static void loadTranscript(){
//		convert video to audio
//		VideoUtils videoUtils = new VideoUtils();
//		videoUtils.convertVideoToAudio("src/main/video/The Entire History Of Cristiano Ronaldo.mp4");


		String audioPath = "videoInfo.mp3";

		try{
			var client = AssemblyAI.builder()
					.apiKey(apiKey)
					.build();

// 			for first time upload file
//			var uploadedFile = client.files().upload(new File(audioPath));
//			var fileUrl = uploadedFile.getUploadUrl();

// 			for after first time, use the uploaded file url, get from log
			var fileUrl = "https://cdn.assemblyai.com/upload/0aa5a092-dee1-4254-b93e-e38aeba553ce";

			var transcriptionParams = TranscriptParams.builder()
					.audioUrl(fileUrl)
					.autoChapters(true)
					.build();

			var transcript = client.transcripts().transcribe(transcriptionParams);


			if (transcript.getStatus() == TranscriptStatus.ERROR) {
				throw new Exception("Transcript failed with error: " + transcript.getError().get());
			}

			var sentences = client.transcripts().getSentences(transcript.getId());
			var paragraphs = client.transcripts().getParagraphs(transcript.getId());

//			print by chapters
//			List<Chapter> chapters = transcript.getChapters().get();
//			chapters.forEach(chapter -> {
//				System.out.println(chapter.getStart() + " - " + chapter.getEnd());
//			});

//			System.out.println(transcript.getText().get().substring(0, 20)));


//			save transcript to file
			Path outputPath = Paths.get("audioTranscript.json");
			Path sentencesOutputPath = Paths.get("sentences" + "AudioTranscript.json");
			Path paragraphsOutputPath = Paths.get("paragraphs" + "AudioTranscript.json");
			try{
//				Files.writeString(outputPath,transcript.toString(), StandardCharsets.UTF_8);
				Files.writeString(sentencesOutputPath,sentences.getSentences().toString(), StandardCharsets.UTF_8);
				Files.writeString(paragraphsOutputPath,paragraphs.getParagraphs().toString(), StandardCharsets.UTF_8);
			}
			catch (Exception e) {
				System.out.println("Invalid path");
				System.out.println("Error:" + e.getMessage());
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
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
