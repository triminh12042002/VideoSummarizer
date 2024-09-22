package com.thelaziest.VideoSummarizer.video;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.requests.TranscriptParams;
import com.assemblyai.api.resources.transcripts.types.TranscriptStatus;
import com.thelaziest.VideoSummarizer.VideoSummarizerApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class VideoUtils {

    @Value("${assemblyai.api.key}")
    private String apiKey;

    public String convertVideoToAudio(String videoPath){
        Runtime runtime = Runtime.getRuntime();
        File videoFile = new File(videoPath);
        String outputPath = "src/main/resources/video/audio/" + videoFile.getName().split("\\.")[0] + ".mp3";

        System.out.println(outputPath);

        String[] command = {"ffmpeg", "-y", "-i", videoPath, "-vn", "-ar", "44100", "-ac", "2", "-b:a", "256k", "-f", "mp3", outputPath};
        try {
            Process process = runtime.exec(command);
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Read output from command
            String s;

            System.out.println("Here is the standard output of the command:\n");
            while((s = stdInput.readLine()) != null){
                System.out.println(s);
            }

            System.out.println("Here is the standard error of the command (if any):\n");
            while((s = stdError.readLine()) != null){
                System.out.println(s);
            }

            return outputPath;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e + "fail to convert Video To Audio");
        }
    }

    public void loadTranscript(String audioPath){
        try{
            System.out.println("apiKey" + apiKey);
            var client = AssemblyAI.builder()
                    .apiKey(apiKey)
                    .build();

// 			for first time upload file
            var uploadedFile = client.files().upload(new File(audioPath));
            var fileUrl = uploadedFile.getUploadUrl();

            System.out.println("uploaded fileUrl: " + fileUrl);

// 			for after first time, use the uploaded file url, get from log
//			var fileUrl = "https://cdn.assemblyai.com/upload/0aa5a092-dee1-4254-b93e-e38aeba553ce";
//          var messiIsBetter = "https://cdn.assemblyai.com/upload/c6dc96c2-0f0a-45e2-915c-4c10c3c77304";

            var transcriptionParams = TranscriptParams.builder()
                    .audioUrl(fileUrl)
                    .autoChapters(true)
                    .build();

            System.out.println("transcribe transcriptionParams");
            var transcript = client.transcripts().transcribe(transcriptionParams);


            if (transcript.getStatus() == TranscriptStatus.ERROR) {
                throw new Exception("Transcript failed with error: " + transcript.getError().get());
            }

            System.out.println("result transcript id: " + transcript.getId());

            var sentences = client.transcripts().getSentences(transcript.getId());
            var paragraphs = client.transcripts().getParagraphs(transcript.getId());

//			print by chapters
//			List<Chapter> chapters = transcript.getChapters().get();
//			chapters.forEach(chapter -> {
//				System.out.println(chapter.getStart() + " - " + chapter.getEnd());
//			});

//			System.out.println(transcript.getText().get().substring(0, 20)));


//			save transcript to file
//            id = afb7a759-17c5-4540-9a26-3f3c47bd136d
            Path outputPath = Paths.get("audioTranscript.json");
            Path sentencesOutputPath = Paths.get("sentences" + "AudioTranscript.json");
            Path paragraphsOutputPath = Paths.get("paragraphs" + "AudioTranscript.json");
            try{
                Files.writeString(outputPath,transcript.toString(), StandardCharsets.UTF_8);
                System.out.println("outputPath finish writeString");
                Files.writeString(sentencesOutputPath,sentences.getSentences().toString(), StandardCharsets.UTF_8);
                System.out.println("sentencesOutputPath finish writeString");
                Files.writeString(paragraphsOutputPath,paragraphs.getParagraphs().toString(), StandardCharsets.UTF_8);
                System.out.println("paragraphsOutputPath finish writeString");
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

    public static void cutAndMergeVideoBySentences(String inputPath, String outputPath, List<Paragraph> paragraphs) {

        String start = "0";
        String end = "0";

        StringBuilder selectCommand = new StringBuilder("select=");
        StringBuilder aselectCommand = new StringBuilder("aselect=");

        for(int i = 0; i < paragraphs.size(); i++){

            if(!paragraphs.get(i).getSentences().isEmpty()){

                start =  String.valueOf(paragraphs.get(i).getSentences().get(0).getStart() / 1000f);
                end =  String.valueOf(paragraphs.get(i).getSentences().get(0).getEnd() / 1000f);

//                System.out.println(VideoSummarizerApplication.convertMillisecondsToStringTimeFormat(Integer.parseInt(start)) + "-" + VideoSummarizerApplication.convertMillisecondsToStringTimeFormat(Integer.parseInt(end)));

                String temp = "between(t," + start + "," + end + ")";

//                System.out.println(temp);
                System.out.println(paragraphs.get(i).getSentences().get(0).getText());

                if(paragraphs.size() == 1){
                    temp = "'" + temp + "'";
                } else if(i == 0){
                    temp = "'" + temp;
                } else if (i == paragraphs.size() - 1){
                    temp = "+" + temp + "'";
                } else {
                    temp = "+" + temp;
                }

                selectCommand.append(temp);
                aselectCommand.append(temp);

            }
        }

        selectCommand.append(",setpts=N/FRAME_RATE/TB");
        aselectCommand.append(",asetpts=N/SR/TB");

        String[] command = {"ffmpeg", "-y" ,"-i", inputPath, "-vf" ,selectCommand.toString(), "-af", aselectCommand.toString() ,outputPath};

//      sample comand
//        String[] command = {"ffmpeg", "-y", "-i", inputPath,
//                "-vf", "select='between(t," + start + "," + end + ")+between(t," + start2 + "," + end2 + ")',setpts=N/FRAME_RATE/TB",
//                "-af", "aselect='between(t," + start + "," + end + ")+between(t," + start2 + "," + end2 + ")',asetpts=N/SR/TB",
//                outputPath};
//        System.out.println(command);



//        try {
//            Runtime runtime = Runtime.getRuntime();
//            Process process = runtime.exec(command);
////            int exitCode = process.waitFor();
////            System.out.println("Exit code: " + exitCode);
//
//            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            String s = null;
//            System.out.println("Here is the standard output of the command:\n");
//            while((s = stdInput.readLine()) != null){
//                System.out.println(s);
//            }
//
//            System.out.println("Here is the standard error of the command (if any):\n");
//            while((s = stdError.readLine()) != null){
//                System.out.println(s);
//            }
//
//
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
    }




}
