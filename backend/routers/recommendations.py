from fastapi import APIRouter
from typing import List, Dict, Any

router = APIRouter(tags=["Recommendations & Health Tips"])

@router.get("/recommendations/lens")
def get_lens_recommendations() -> List[Dict[str, Any]]:
    return [
        {
            "condition": "Normal Vision",
            "lens_type": "Anti-Glare & Blue Cut Lens",
            "badge": "Daily Protection",
            "description": "Premium multi-layer anti-reflective coating designed to eliminate glare and reduce digital eye strain.",
            "benefits": [
                "Filters 95%+ of harmful blue light emitted by digital screens",
                "Eliminates reflective glare for superior contrast",
                "Hydrophobic and oleophobic smudge-resistant coating"
            ],
            "ideal_for": "Computer users, mobile users, and night drivers"
        },
        {
            "condition": "Myopia",
            "lens_type": "Single Vision Concave (Minus) Lens",
            "badge": "Distance Correction",
            "description": "High-definition divergence lenses calibrated to refocus distance rays directly onto your retina.",
            "benefits": [
                "Sharp and crisp distance vision restoration",
                "Ultra-thin high index (1.67/1.74) for lightweight wear",
                "100% UV400 ultraviolet solar radiation protection"
            ],
            "ideal_for": "Nearsighted individuals requiring distance focus"
        },
        {
            "condition": "Hyperopia",
            "lens_type": "Single Vision Convex (Plus) Lens",
            "badge": "Reading & Near Focus",
            "description": "Precision convergence optical lenses designed to assist focusing on close-up text and intricate tasks.",
            "benefits": [
                "Relieves eye fatigue and brow ache during reading",
                "Aspheric edge-to-edge optical clarity",
                "Scratch-resistant sapphire-grade hard coating"
            ],
            "ideal_for": "Farsighted individuals and reading tasks"
        },
        {
            "condition": "Astigmatism",
            "lens_type": "Precision Toric Cylindrical Lens",
            "badge": "Curvature Alignment",
            "description": "Dual-meridian custom toric curvature designed to correct irregular corneal astigmatism distortion.",
            "benefits": [
                "Corrects distorted / stretched peripheral focal planes",
                "Stabilized rotational axis technology",
                "Advanced anti-fatigue comfort layer"
            ],
            "ideal_for": "Individuals with blurred vision at all distances"
        }
    ]

@router.get("/tips/eye-health")
def get_eye_health_tips() -> List[Dict[str, Any]]:
    return [
        {
            "id": 1,
            "title": "Master the 20-20-20 Rule",
            "category": "Digital Wellness",
            "description": "Every 20 minutes of screen time, look at an object at least 20 feet away for 20 seconds to relax your eye muscles.",
            "icon": "ic_timer"
        },
        {
            "id": 2,
            "title": "Optimal Lighting & Contrast",
            "category": "Workspace Ergonomics",
            "description": "Never use screens in complete darkness. Match room lighting to screen luminance to avoid intense pupil constriction.",
            "icon": "ic_brightness"
        },
        {
            "id": 3,
            "title": "Blink Frequently & Stay Hydrated",
            "category": "Corneal Health",
            "description": "Blink rate drops by 50% during screen time. Consciously blink and drink plenty of water to maintain tear film moisture.",
            "icon": "ic_water"
        },
        {
            "id": 4,
            "title": "Nutrients for Vision Longevity",
            "category": "Nutrition",
            "description": "Consume foods rich in Lutein, Zeaxanthin, Omega-3 fatty acids, and Vitamin A (carrots, spinach, citrus, salmon).",
            "icon": "ic_food"
        },
        {
            "id": 5,
            "title": "Annual Comprehensive Exam",
            "category": "Clinical Care",
            "description": "Schedule a yearly dilated eye exam with an eye care professional to monitor intraocular pressure and retinal health.",
            "icon": "ic_doctor"
        }
    ]
