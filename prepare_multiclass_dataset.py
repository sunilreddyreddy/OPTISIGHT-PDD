"""
OptiSight AI - Multi-Class Eye Disease Dataset Preparer & Trainer
Automatically converts class-based folders into YOLO multi-class format and trains the AI.

Supported Directory Structure:
Dataset/
├── Normal/
├── Cataract/
├── Glaucoma/
├── Diabetic_Retinopathy/
├── Myopia/
├── Astigmatism/
├── Hypertension/
└── AMD/
"""

import os
import shutil
import random
from pathlib import Path
from ultralytics import YOLO

BASE_DIR = Path(__file__).resolve().parent
DATASET_DIR = BASE_DIR / "Dataset"
TRAIN_IMG = DATASET_DIR / "train" / "images"
TRAIN_LBL = DATASET_DIR / "train" / "labels"
VAL_IMG = DATASET_DIR / "val" / "images"
VAL_LBL = DATASET_DIR / "val" / "labels"

def prepare_yolo_dataset():
    # 1. Discover class folders inside Dataset/
    excluded = {"train", "val", "test", "labels", "images", "downloads"}
    class_folders = [f for f in DATASET_DIR.iterdir() if f.is_dir() and f.name.lower() not in excluded]
    
    if not class_folders:
        print("[Notice] No class subfolders found in Dataset/.")
        print("To train multi-class, create folders for each disease inside Dataset/:")
        print("  Dataset/Normal/")
        print("  Dataset/Cataract/")
        print("  Dataset/Glaucoma/")
        print("  Dataset/Diabetic_Retinopathy/")
        print("  Dataset/Myopia/")
        print("  Dataset/Astigmatism/")
        print("  Dataset/AMD/")
        return False, []

    class_names = sorted([f.name for f in class_folders])
    print(f"[Dataset] Found {len(class_names)} classes: {class_names}")

    # Recreate train/val folders
    for p in [TRAIN_IMG, TRAIN_LBL, VAL_IMG, VAL_LBL]:
        p.mkdir(parents=True, exist_ok=True)

    # 2. Convert and split images
    valid_exts = {".jpg", ".jpeg", ".png", ".bmp", ".webp"}
    total_train = 0
    total_val = 0

    for cls_idx, folder in enumerate(class_folders):
        cls_name = folder.name
        images = [f for f in folder.iterdir() if f.suffix.lower() in valid_exts]
        random.shuffle(images)

        split_idx = int(len(images) * 0.8)
        train_imgs = images[:split_idx]
        val_imgs = images[split_idx:]

        for img in train_imgs:
            dest_img = TRAIN_IMG / f"{cls_name}_{img.name}"
            shutil.copy2(img, dest_img)
            # Create full-frame bounding box label for classification
            lbl_file = TRAIN_LBL / f"{dest_img.stem}.txt"
            with open(lbl_file, "w") as f:
                f.write(f"{cls_idx} 0.5 0.5 1.0 1.0\n")
            total_train += 1

        for img in val_imgs:
            dest_img = VAL_IMG / f"{cls_name}_{img.name}"
            shutil.copy2(img, dest_img)
            lbl_file = VAL_LBL / f"{dest_img.stem}.txt"
            with open(lbl_file, "w") as f:
                f.write(f"{cls_idx} 0.5 0.5 1.0 1.0\n")
            total_val += 1

    # 3. Generate data.yaml
    yaml_path = DATASET_DIR / "data.yaml"
    yaml_content = f"""path: {DATASET_DIR.as_posix()}
train: train/images
val: val/images
test: val/images

nc: {len(class_names)}
names: {class_names}
"""
    with open(yaml_path, "w") as f:
        f.write(yaml_content)

    print(f"[Dataset Ready] Prepared {total_train} train images and {total_val} validation images across {len(class_names)} classes.")
    return True, class_names

def train(class_names):
    print("\n[OptiSight AI] Starting Multi-Class Training on YOLO11...")
    model = YOLO("yolo11n.pt")
    results = model.train(
        data=str(DATASET_DIR / "data.yaml"),
        epochs=30,
        imgsz=640,
        device="cpu",
        project="OptiSightModel",
        name="multiclass_vision_analysis"
    )
    print("\n[OptiSight AI] Multi-Class Training Complete!")
    print(f"Model saved to: runs/detect/OptiSightModel/multiclass_vision_analysis/weights/best.pt")

if __name__ == "__main__":
    success, classes = prepare_yolo_dataset()
    if success:
        train(classes)
