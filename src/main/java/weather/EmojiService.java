package weather;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

public class EmojiService {
    public Emoji getEmojiForWeather(String iconId) {
        Emoji emoji = null;

        String s = iconId;
        switch (s) {
            case "01n":
            case "01d":
                emoji = EmojiManager.getForAlias( "sun_with_face" );
                break;
            case "02n":
            case "02d":
                emoji = EmojiManager.getForAlias( "white_sun_behind_cloud" );
                break;
            case "03n":
            case "03d":
            case "04n":
            case "04d":
                emoji = EmojiManager.getForAlias( "cloud" );
                break;
            case "09n":
            case "09d":
                emoji = EmojiManager.getForAlias( "umbrella" );
                break;
            case "10n":
            case "10d":
                emoji = EmojiManager.getForAlias( "cloud_rain" );
                break;
            case "11n":
            case "11d":
                emoji = EmojiManager.getForAlias( "thunder_cloud_rain" );
                break;
            case "13n":
            case "13d":
                emoji = EmojiManager.getForAlias( "snowflake" );
                break;
            case "50n":
            case "50d":
                emoji = EmojiManager.getForAlias( "fog" );
                break;
            case "globe":
                emoji = EmojiManager.getForAlias( "earth_africa" );
                break;
            case "diamond":
                emoji = EmojiManager.getForAlias( "small_blue_diamond" );
                break;
            default:
                emoji = null;
                break;
        }

        return emoji;
    }
}
