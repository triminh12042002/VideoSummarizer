package com.thelaziest.VideoSummarizer.video;

import com.thelaziest.VideoSummarizer.VideoSummarizerApplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class VideoUtils {
    public void convertVideoToAudio(String videoPath){
        Runtime runtime = Runtime.getRuntime();
        String outputPath = "videoInfo.mp3";

//        String[] command = {"ffmpeg", "-i", videoPath };
//        String command = "ffmpeg -i " + videoPath;
        String[] command = {"ffmpeg", "-y", "-i", videoPath, "-vn", "-ar", "44100", "-ac", "2", "-b:a", "256k", "-f", "mp3", outputPath};
        try {
            Process process = runtime.exec(command);
            int exitCode = process.waitFor();
            System.out.println("Exit code: " + exitCode);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // Read output from command
            String s = null;

            System.out.println("Here is the standard output of the command:\n");
            while((s = stdInput.readLine()) != null){
                System.out.println(s);
            }

            System.out.println("Here is the standard error of the command (if any):\n");
            while((s = stdError.readLine()) != null){
                System.out.println(s);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cutAndMergeVideoBySentences(String inputPath, String outputPath, List<Paragraph> paragraphs) {

//        String[] command = {"ffmpeg", "-i", inputPath, "-ss", "00:01:02.500", "-t", "00:01:03.250", "-c", "copy", outputPath};
        String start = "0";
        String end = "0";
        outputPath = "outputHighlightsOfTheEntireHistoryOfRonaldo.mp4";

        StringBuilder selectCommand = new StringBuilder("select=");
        StringBuilder aselectCommand = new StringBuilder("aselect=");

        for(int i = 0; i < paragraphs.size(); i++){

            if(!paragraphs.get(i).getSentences().isEmpty()){

                start =  String.valueOf(paragraphs.get(i).getSentences().get(0).getStart() / 1000f);
                end =  String.valueOf(paragraphs.get(i).getSentences().get(0).getEnd() / 1000f);

//                System.out.println(VideoSummarizerApplication.convertMillisecondsToStringTimeFormat(Integer.parseInt(start)) + "-" + VideoSummarizerApplication.convertMillisecondsToStringTimeFormat(Integer.parseInt(end)));

                String temp = "between(t," + start + "," + end + ")";

//                System.out.println(temp);
//                System.out.println(paragraphs.get(i).getSentences().get(0).getText());

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
