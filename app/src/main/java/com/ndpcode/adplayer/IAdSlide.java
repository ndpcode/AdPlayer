//*********************************************************************************************************//
//Interface for one slide of AdPlayer
//Created 03.06.2022
//Created by Novikov Dmitry
//*********************************************************************************************************//

package com.ndpcode.adplayer;

import java.util.concurrent.Exchanger;

public interface IAdSlide extends Runnable {
    Boolean Load(String sourcePath, Exchanger<Integer> exchanger);
}
