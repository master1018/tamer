package com.moviejukebox.tvrage;

import com.moviejukebox.tvrage.model.Episode;
import com.moviejukebox.tvrage.model.EpisodeList;
import com.moviejukebox.tvrage.model.ShowInfo;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class TVRageTest {

    private static String apikey = "";

    private TVRage tvr;

    private static final String SHOW_ID_STR = "15614";

    private static final int SHOW_ID_INT = 15614;

    private static final String SHOW_NAME = "Chuck";

    @Before
    public void setUp() throws Exception {
        tvr = new TVRage(apikey);
    }

    @Test
    public void testGetEpisodeInfo() {
        Episode episode = tvr.getEpisodeInfo(SHOW_ID_STR, "1", "1");
        assertTrue(episode.getTitle().equals("Pilot"));
    }

    @Test
    public void testGetEpisodeList() {
        EpisodeList episodeList = tvr.getEpisodeList(SHOW_ID_STR);
        assertFalse(episodeList.getShowName().equals(TVRage.UNKNOWN));
        assertFalse(episodeList.getEpisodeList().isEmpty());
    }

    @Test
    public void testGetShowInfoInt() {
        ShowInfo showInfo = tvr.getShowInfo(SHOW_ID_INT);
        assertFalse(showInfo.getShowName().equals(TVRage.UNKNOWN));
    }

    @Test
    public void testGetShowInfoString() {
        ShowInfo showInfo = tvr.getShowInfo(SHOW_ID_STR);
        assertFalse(showInfo.getShowName().equals(TVRage.UNKNOWN));
    }

    @Test
    public void testSearchShow() {
        boolean found = false;
        List<ShowInfo> showList = tvr.searchShow(SHOW_NAME);
        for (ShowInfo showInfo : showList) {
            if (showInfo.getShowName().equalsIgnoreCase(SHOW_NAME)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
}
