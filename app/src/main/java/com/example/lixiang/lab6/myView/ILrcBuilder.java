package com.example.lixiang.lab6.myView;

import com.example.lixiang.lab6.myView.LrcRow;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public interface ILrcBuilder {
    InputStream setDataSource(String path);
    String readStreamToString(InputStream inputStream, Charset charset);
    List<LrcRow> getLrcRows(String rawLrc);
}
