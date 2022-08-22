package com.ndpcode.adplayer;

import java.util.concurrent.Exchanger;

public interface IAdSlide extends Runnable {
    Boolean Load(String sourcePath, Exchanger<Integer> exchanger);
}
