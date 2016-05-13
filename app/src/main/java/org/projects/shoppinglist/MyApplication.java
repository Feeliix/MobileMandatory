package org.projects.shoppinglist;
import com.firebase.client.Firebase;
/**
 * Created by felix on 25-04-2016.
 */
public class MyApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

    }
}
