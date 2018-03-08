package ext.android.arch.component.stacks;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Iterator;
import java.util.Stack;

import ext.java8.function.Predicate;

/**
 * trace Activity instance.
 * it will be Ignored if Activity Annotation with {@link ext.android.arch.component.annotations.NonStack}
 */
public final class ActivityStack {

    private static volatile ActivityStack _instance;

    private Stack<Activity> mStack;

    public static ActivityStack get() {
        if (_instance == null) {
            synchronized (ActivityStack.class) {
                if (_instance == null) {
                    _instance = new ActivityStack();
                }
            }
        }
        return _instance;
    }

    private ActivityStack() {
        mStack = new Stack<>();
    }

    public void push(@NonNull Activity activity) {
        if (!mStack.contains(activity)) {
            mStack.push(activity);
        }
    }

    public void pop(@NonNull Activity activity) {
        mStack.remove(activity);
        if (isActivityAvailable(activity)) {
            activity.finish();
        }
    }

    public void pop(@NonNull Class<? extends Activity> cls) {
        popInternal(aClass -> cls.equals(aClass));
    }

    public void popAllIgnore(@NonNull Class<? extends Activity> cls) {
        popInternal(aClass -> !aClass.equals(cls));
    }

    public void popAll() {
        popInternal(null);
    }

    public Activity top() {
        return mStack.lastElement();
    }

    private void popInternal(@Nullable Predicate<Class<? extends Activity>> filter) {
        Iterator<Activity> iterator = mStack.iterator();
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            if (activity == null) {
                continue;
            }
            if (filter == null || filter.test(activity.getClass())) {
                if (isActivityAvailable(activity)) {
                    activity.finish();
                }
                iterator.remove();
            }
        }
    }

    private static boolean isActivityAvailable(@NonNull Activity activity) {
        return !activity.isDestroyed() && !activity.isFinishing();
    }
}