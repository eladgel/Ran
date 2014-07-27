package com.danbt.loaders;

import android.content.Context;
import android.os.SystemClock;
import android.content.AsyncTaskLoader;

import com.danbt.simple.Constants;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by eladgelman on 7/25/14.
 */

public class BTAsyncTaskLoader extends AsyncTaskLoader<String>
{

    private long lastGuiUpdate;
    private String msg;

    public BTAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public String loadInBackground() {
        String retVal = null;
        if(msg != null){
            if ((SystemClock.elapsedRealtime() - lastGuiUpdate) > 1500) {
//            lastMessage = msg;//lastMessage + msg;
                if ((msg.length() - msg.replaceAll("E", "")
                        .length()) >= 3) {
                    String tokens[] = Pattern.compile("E").split(msg);
                    for (int i = 0; i < tokens.length; i++) {
                        if (!Pattern.matches(Constants.STRING_FORMAT, tokens[i])) {
                            tokens[i] = "";
                        }
                    }
                    Arrays.sort(tokens);
                    for (int i = 0; i <= tokens.length - 3; i++) {
                        if (tokens[i].compareTo("") != 0
                                && tokens[i + 1].compareTo(tokens[i]) == 0
                                && tokens[i + 2].compareTo(tokens[i]) == 0) {
//                            readAndValidateValues(tokens[i]);
                            retVal = tokens[i];
                            break;
                        }
                    }
                }

            }
        }


        return retVal;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
//        forceLoad();
    }

    @Override
    public void deliverResult(String data) {
        super.deliverResult(data);
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setLastGuiUpdate(long lastGuiUpdate) {
        this.lastGuiUpdate = lastGuiUpdate;
    }
}