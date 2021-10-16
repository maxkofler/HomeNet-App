package sdt.maxkofler.homenet_app.homenet.homenet;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

public class HNParser {
    private static final String cN = "HomeNet-App:HNParser";

    private Vector<Vector<String>> contents;

    public HNParser(){
    }

    public boolean parse(BufferedReader in){
        {//Set up environment
            this.contents = new Vector<>();
        }

        if (in == null){
            Log.w(cN + ".parse()", "No valid BufferedReader provided!");
            return false;
        }


        {//Parse one
            try{
                String curLine = in.readLine();
                String[] blocks;

                while (curLine != null){
                    blocks = curLine.split("<*>");
                    for (int i = 0; i < blocks.length; i++){
                        blocks[i] = blocks[i].replace("<", "");
                        blocks[i] = blocks[i].replace(">", "");
                        //@NETWORKING_LOG
                        System.out.print("[" + blocks[i] + "]");
                    }
                    //@NETWORKING_LOG
                    System.out.println();

                    {
                        Vector<String> newLine = new Vector<>();
                        for (String block : blocks){
                            newLine.add(block);
                        }
                        this.contents.add(newLine);
                    }


                    curLine = in.readLine();
                }

            }catch (IOException e){

            }

        }
        return true;
    }

    public Vector<Vector<String>> getBlocks(){
        return this.contents;
    }
}
