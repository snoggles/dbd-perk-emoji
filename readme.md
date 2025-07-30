# dbd-perk-emoji
Converts DBD perk icons into discord emoji images.

## Motivation
The perk diamond shape is too small to see when scaled down to Discord emoji size. This project generates square images with a small border. Some icons are manually edited to crop out unimportant details.

## Workflow
1. Edit images in [images/input](images/input), e.g. crop or remove details for clarity at small sizes.
2. Run `mill emoji.run`
3. Review the templated images in [images/output](images/output).
4. Upload the ones you want to discord.

## Originals
Find original perk icons in [Ultimate Dead by Daylight Assets Collection](https://drive.google.com/drive/folders/1XA6ps4xS-UtGojM_wedE5ptRHCTkmzEV).
