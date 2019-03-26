package project.kym.mychat.util;

import android.os.Debug;

/** 코드 속도 측정 클래스 */
public class CheackTaskTime {
    long threadCpuTimeNanosStart = -1;
    long threadCpuTimeNanosEnd = -1;
    long result = -1;
    // 생성과 동시에 시작.
    public CheackTaskTime(){
        threadCpuTimeNanosStart = Debug.threadCpuTimeNanos();
    }

    public void start(){
        threadCpuTimeNanosStart = Debug.threadCpuTimeNanos();
    }

    public String end(){
        threadCpuTimeNanosEnd = Debug.threadCpuTimeNanos();
        result = (threadCpuTimeNanosEnd - threadCpuTimeNanosStart) / 1000;
        if(result > 10000)
            return ""+ result/1000 + " 밀리초";
        else
            return ""+ result + " 마이크로초";
    }

    public String getResult(){
        if(result > 10000)
            return ""+ result/1000 + " 밀리초";
        else
            return ""+ result + " 마이크로초";
    }
}
