package com.example.zoom;

import com.example.zoom.util.RandomUtil;

public class CreateSessionActivity extends BaseSessionActivity {

    protected String getDefaultSessionName() {
        String defaultName = super.getDefaultSessionName();
        return defaultName + "_" + RandomUtil.getEightRandom();
    }

    @Override
    protected void init() {
        super.init();
        setHeadTile(R.string.create_title);
    }

}
