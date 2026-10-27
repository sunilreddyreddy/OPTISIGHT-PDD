"""
OptiSight AI - Multi-Class Vision Analysis Pipeline
Builds a curated dataset from local downloads & existing dataset and trains YOLO11 on:
1. Normal
2. Myopia
3. Eye Disease
"""

import os
import shutil
import zipfile
from pathlib import Path
from ultralytics import YOLO

BASE_DIR = Path(__file__).resolve().parent
DATASET_DIR = BASE_DIR / "Dataset"
TRAIN_IMG = DATASET_DIR / "train" / "images"
TRAIN_LBL = DATASET_DIR / "train" / "labels"
VAL_IMG = DATASET_DIR / "val" / "images"
VAL_LBL = DATASET_DIR / "val" / "labels"

CLASSES = ["Normal", "Myopia", "Eye Disease"]

def setup_multiclass_dataset():
    print("[1/3] Preparing Multi-Class Dataset directories...")
    # Clear existing train/val
    for d in [TRAIN_IMG, TRAIN_LBL, VAL_IMG, VAL_LBL]:
        if d.exists():
            shutil.rmtree(d)
        d.mkdir(parents=True, exist_ok=True)

    # 1. Extract Myopia and Normal from Downloads
    myopia_zip = Path("C:/Users/Samsung/Downloads/myopia dataset.zip")
    if myopia_zip.exists():
        print(f"[2/3] Extracting curated images from {myopia_zip.name}...")
        with zipfile.ZipFile(myopia_zip, "r") as zf:
            myopia_files = [n for n in zf.namelist() if "Myopia_images" in n and n.lower().endswith((".png", ".jpg", ".jpeg"))][:350]
            normal_files = [n for n in zf.namelist() if "Normal_images" in n and n.lower().endswith((".png", ".jpg", ".jpeg"))][:350]

            # Process Normal (Class 0)
            split_norm = int(len(normal_files) * 0.8)
            for i, name in enumerate(normal_files):
                dest_dir = TRAIN_IMG if i < split_norm else VAL_IMG
                lbl_dir = TRAIN_LBL if i < split_norm else VAL_LBL
                
                target_img = dest_dir / f"norm_{Path(name).name}"
                with open(target_img, "wb") as f:
                    f.write(zf.read(name))
                
                with open(lbl_dir / f"{target_img.stem}.txt", "w") as lf:
                    lf.write("0 0.5 0.5 1.0 1.0\n")

            # Process Myopia (Class 1)
            split_myo = int(len(myopia_files) * 0.8)
            for i, name in enumerate(myopia_files):
                dest_dir = TRAIN_IMG if i < split_myo else VAL_IMG
                lbl_dir = TRAIN_LBL if i < split_myo else VAL_LBL
                
                target_img = dest_dir / f"myo_{Path(name).name}"
                with open(target_img, "wb") as f:
                    f.write(zf.read(name))
                
                with open(lbl_dir / f"{target_img.stem}.txt", "w") as lf:
                    lf.write("1 0.5 0.5 1.0 1.0\n")

    # 2. Extract Eye Disease from raw test dataset
    raw_test_dir = DATASET_DIR / "test" / "images"
    if raw_test_dir.exists():
        disease_files = [f for f in raw_test_dir.glob("*.jpg")][:350]
        split_dis = int(len(disease_files) * 0.8)
        for i, img_path in enumerate(disease_files):
            dest_dir = TRAIN_IMG if i < split_dis else VAL_IMG
            lbl_dir = TRAIN_LBL if i < split_dis else VAL_LBL
            
            target_img = dest_dir / f"dis_{img_path.name}"
            shutil.copy2(img_path, target_img)
            
            with open(lbl_dir / f"{target_img.stem}.txt", "w") as lf:
                lf.write("2 0.5 0.5 1.0 1.0\n")

    # 3. Write data.yaml
    yaml_content = f"""path: {DATASET_DIR.as_posix()}
train: train/images
val: val/images
test: val/images

nc: {len(CLASSES)}
names: {CLASSES}
"""
    with open(DATASET_DIR / "data.yaml", "w") as f:
        f.write(yaml_content)

    train_count = len(list(TRAIN_IMG.glob("*.*")))
    val_count = len(list(VAL_IMG.glob("*.*")))
    print(f"[Dataset Configured] Total: {train_count} Train Images | {val_count} Val Images across {len(CLASSES)} classes: {CLASSES}")

def train_multiclass_model():
    print("\n[3/3] Training YOLO11 Multi-Class Neural Network on CPU...")
    model = YOLO("yolo11n.pt")
    results = model.train(
        data=str(DATASET_DIR / "data.yaml"),
        epochs=20,
        imgsz=640,
        batch=16,
        device="cpu",
        project="OptiSightModel",
        name="multiclass_vision_analysis"
    )
    print("\n==========================================")
    print("🎉 Multi-Class Model Training Completed Successfully!")
    print("Trained Weights: runs/detect/OptiSightModel/multiclass_vision_analysis/weights/best.pt")
    print("==========================================")

if __name__ == "__main__":
    setup_multiclass_dataset()
    train_multiclass_model()
