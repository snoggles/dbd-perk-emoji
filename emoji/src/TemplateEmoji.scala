import com.sksamuel.scrimage.*
import com.sksamuel.scrimage.composite.AlphaComposite
import com.sksamuel.scrimage.nio.PngWriter

import java.awt.Color

object TemplateEmoji:
  private val imagesPath = os.pwd / "images"
  private val inputPath = imagesPath / "input"
  private val outputPath = imagesPath / "output"
  private val purpleBgImg = ImmutableImage.loader().fromFile((imagesPath / "purple-bg.png").toIO)
  private val overlay = AlphaComposite(1)

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
    addBlackBorder(addPurpleBg(padToSquare(cropTight(source))))

  private def cropTight(img: ImmutableImage): ImmutableImage =
    val xs = 0 until img.width
    val ys = 0 until img.height

    def isPerkPixel(x: Int, y: Int) =
      val pixel = img.pixel(x, y)
      val alpha = pixel.alpha()
      alpha >= (0.9 * 255)

    val xLeft = xs.find(x => ys.exists(y => isPerkPixel(x, y))).get
    val yTop = ys.find(y => xs.exists(x => isPerkPixel(x, y))).get
    val xRight = xs.findLast(x => ys.exists(y => isPerkPixel(x, y))).get
    val yBottom = ys.findLast(y => xs.exists(x => isPerkPixel(x, y))).get

    val width = xRight - xLeft + 1
    val height = yBottom - yTop + 1

    img.subimage(xLeft, yTop, width, height)

  private def padToSquare(img: ImmutableImage): ImmutableImage =
    val perkSize = math.max(img.width, img.height)
    img.resizeTo(perkSize, perkSize, Position.Center)

  private def addPurpleBg(img: ImmutableImage): ImmutableImage =
    purpleBgImg.scaleTo(img.width, img.height).composite(overlay, img)

  private def addBlackBorder(img: ImmutableImage): ImmutableImage =
    require(img.width == img.height)
    val borderThickness = math.round(0.05 * img.width).toInt
    val compositeSize = img.width + borderThickness * 2
    val blackBg = ImmutableImage.create(compositeSize, compositeSize).fill(Color.black)
    val padded = img.resizeTo(compositeSize, compositeSize, Position.Center)
    blackBg.composite(overlay, img)

  private def toEmojiName(fileName: String): String =
    fileName
      .replaceAll("[^A-Za-z ]", "")
      .split("""\s+""")
      .map(_.toLowerCase.capitalize)
      .mkString("z", "", "")
