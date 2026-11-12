package com.cs364.roastapp.roast;

public interface Level3Alert {

    void onLevel3(String packageName, long millisOver);

    void stop(String packageName);
}
