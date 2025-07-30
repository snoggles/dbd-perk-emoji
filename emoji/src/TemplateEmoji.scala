import com.sksamuel.scrimage.*
import com.sksamuel.scrimage.composite.AlphaComposite
import com.sksamuel.scrimage.nio.PngWriter

import java.awt.Color

object TemplateEmoji:
  private val imagesPath = os.pwd / "images"
  private val inputPath = imagesPath / "input"
  private val outputPath = imagesPath / "output"
  private val purpleBgImg = ImmutableImage.loader().fromFile((imagesPath / "purple-bg.png").toIO)
  private val composite = AlphaComposite(1)

  @main def run(): Unit =
    for
      file <- os.list(inputPath) if file.ext == "png"
    do
      val sourceImage = ImmutableImage.loader().fromFile(file.toIO)
      val emojiImage = emojifyImage(sourceImage)

      val emojiName = toEmojiName(file.baseName)
      val destPath = outputPath / s"$emojiName.png"

      emojiImage.output(PngWriter.MaxCompression, destPath.toIO)

  private def emojifyImage(source: ImmutableImage): ImmutableImage =
    // Trim the whitespace
    var img = cropTight(source)
    val perkSize = math.max(img.width, img.height)
    img = img.resizeTo(perkSize, perkSize, Position.Center)

    // Apply the purple background
    img = purpleBgImg.scaleTo(perkSize, perkSize).composite(composite, img)

    // Add a black border
    val borderThickness = math.round(0.05 * perkSize).toInt
    val compositeSize = perkSize + borderThickness * 2
    val blackBg = ImmutableImage.create(compositeSize, compositeSize).fill(Color.black)
    img = img.resizeTo(compositeSize, compositeSize, Position.Center)
    img = blackBg.composite(composite, img)
    img

  private def toEmojiName(fileName: String): String =
    fileName
      .replaceAll("[^A-Za-z ]", "")
      .split("""\s+""")
      .map(_.toLowerCase.capitalize)
      .mkString("z", "", "")

  private def cropTight(img: ImmutableImage): ImmutableImage =
    val xs = 0 until img.width
    val ys = 0 until img.height

    def isPerkPixel(x: Int, y: Int) =
      val pixel = img.pixel(x, y)
      val alpha = pixel.alpha()
      alpha >= (0.9 * 255)

    val xLeft = xs.find(x => ys.exists(y => isPerkPixel(x, y))).get
    val yTop = ys.find(y => xs.exists(x => isPerkPixel(x, y))).get

    val xRight = xs.reverseIterator.find(x => ys.exists(y => isPerkPixel(x, y))).get
    val yBottom = ys.reverseIterator.find(y => xs.exists(x => isPerkPixel(x, y))).get

    img.subimage(xLeft, yTop, xRight - xLeft + 1, yBottom - yTop + 1)

