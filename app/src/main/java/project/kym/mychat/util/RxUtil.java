package project.kym.mychat.util;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class RxUtil {
    public static void simpleTask(Object object, SimpleTask simpleTask){
        Observable.just(object).map(new Function<Object, Object>() {
            @Override
            public Object apply(Object o) throws Exception {
                return simpleTask.backTask();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                simpleTask.uiTask(o);
            }
        });
    }

    public interface SimpleTask<T>{
        T backTask();
        void uiTask(T result);
    }
}
