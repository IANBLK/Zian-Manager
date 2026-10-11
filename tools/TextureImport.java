import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.nio.file.Path;
/** Technical nearest-neighbor import into a power-of-two Minecraft texture. */
public class TextureImport {
    public static void main(String[] args) throws Exception {
        var source=ImageIO.read(Path.of(args[0]).toFile());
        var texture=new BufferedImage(1024,1024,BufferedImage.TYPE_INT_ARGB);
        var graphics=texture.createGraphics();graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(source,0,0,1024,1024,null);graphics.dispose();ImageIO.write(texture,"PNG",Path.of(args[1]).toFile());
    }
}
