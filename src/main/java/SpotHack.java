/**
 * Created by JP on 9/26/2015.
 */
import static spark.Spark.*;
public class SpotHack {
    public static void main(String[] args) {
        get("/", (req, res) -> "ay lmao");
    }
}
