package weather;

import com.vdurmont.emoji.EmojiManager;

public class EmojiService {
    public String getEmojiForWeather(String iconId) {
        String emoji = null;

        String s = iconId;
        switch (s) {
            case "01n":
            case "01d":
                emoji = EmojiManager.getForAlias( "sun_with_face" ).getUnicode();
                break;
            case "02n":
            case "02d":
                emoji = EmojiManager.getForAlias( "white_sun_behind_cloud" ).getUnicode();
                break;
            case "03n":
            case "03d":
            case "04n":
            case "04d":
                emoji = EmojiManager.getForAlias( "cloud" ).getUnicode();
                break;
            case "09n":
            case "09d":
                emoji = EmojiManager.getForAlias( "umbrella" ).getUnicode();
                break;
            case "10n":
            case "10d":
                emoji = EmojiManager.getForAlias( "cloud_rain" ).getUnicode();
                break;
            case "11n":
            case "11d":
                emoji = EmojiManager.getForAlias( "thunder_cloud_rain" ).getUnicode();
                break;
            case "13n":
            case "13d":
                emoji = EmojiManager.getForAlias( "snowflake" ).getUnicode();
                break;
            case "50n":
            case "50d":
                emoji = EmojiManager.getForAlias( "fog" ).getUnicode();
                break;
            case "globe":
                emoji = EmojiManager.getForAlias( "earth_africa" ).getUnicode();
                break;
            case "diamond":
                emoji = EmojiManager.getForAlias( "small_blue_diamond" ).getUnicode();
                break;
            case "ball":
                emoji = EmojiManager.getForAlias( "crystal_ball" ).getUnicode();
                break;
            case "satellite":
                emoji = EmojiManager.getForAlias( "satellite" ).getUnicode();
                break;
            case "alert":
                emoji = EmojiManager.getForAlias( "alarm_clock" ).getUnicode();
                break;
            case "lang":
                emoji = EmojiManager.getForAlias( "speaker" ).getUnicode();
                break;
            case "noSub":
                emoji = EmojiManager.getForAlias( "no_entry_sign" ).getUnicode();
                break;
            default:
                emoji = null;
                break;
        }

        return emoji;
    }
}
