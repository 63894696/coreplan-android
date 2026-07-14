package com.example.fitness.data

/**
 * 数据层的中文翻译。把数据里英文的 body_part / category / equipment / target / muscle_group 翻成中文。
 * 锁中文的策略：直接 hardcode，未匹配到的英文原文显示（不该发生，但兜底）。
 */
object DataTranslations {

    fun bodyPart(name: String): String = mapBodyPart[name] ?: name
    fun category(name: String): String = mapBodyPart[name] ?: name  // category == body_part
    fun equipment(name: String): String = mapEquipment[name] ?: name
    fun target(name: String): String = mapTarget[name] ?: name
    fun muscleGroup(name: String): String = mapMuscleGroup[name] ?: name

    private val mapBodyPart: Map<String, String> = mapOf(
        "waist" to "腰部",
        "upper legs" to "大腿",
        "back" to "背部",
        "chest" to "胸部",
        "shoulders" to "肩部",
        "upper arms" to "上臂",
        "lower legs" to "小腿",
        "lower arms" to "前臂",
        "cardio" to "有氧",
        "neck" to "颈部"
    )

    private val mapEquipment: Map<String, String> = mapOf(
        "body weight" to "自重",
        "dumbbell" to "哑铃",
        "barbell" to "杠铃",
        "cable" to "绳索",
        "kettlebell" to "壶铃",
        "stability ball" to "健身球",
        "ez barbell" to "EZ 杠",
        "smith machine" to "史密斯机",
        "leverage machine" to "力量器械",
        "band" to "弹力带",
        "resistance band" to "弹力带",
        "weighted" to "负重",
        "olympic barbell" to "奥林匹克杠铃",
        "rope" to "跳绳",
        "medicine ball" to "药球",
        "bosu ball" to "半球平衡垫",
        "roller" to "泡沫轴",
        "wheel roller" to "健腹轮",
        "hammer" to "锤",
        "tire" to "轮胎",
        "sled machine" to "推橇机",
        "stationary bike" to "动感单车",
        "elliptical machine" to "椭圆机",
        "skierg machine" to "滑雪机",
        "stepmill machine" to "踏步机",
        "upper body ergometer" to "上肢测力计",
        "trap bar" to "六角杠",
        "assisted" to "辅助",
        "other" to "其他",
        "none" to "无"
    )

    private val mapTarget: Map<String, String> = mapOf(
        "abs" to "腹肌",
        "quads" to "股四头肌",
        "hamstrings" to "腘绳肌",
        "glutes" to "臀大肌",
        "calves" to "小腿肌",
        "biceps" to "肱二头肌",
        "triceps" to "肱三头肌",
        "forearms" to "前臂",
        "chest" to "胸肌",
        "lats" to "背阔肌",
        "traps" to "斜方肌",
        "shoulders" to "三角肌",
        "delts" to "三角肌",
        "pectorals" to "胸大肌",
        "serratus anterior" to "前锯肌",
        "adductors" to "内收肌",
        "abductors" to "外展肌",
        "spine" to "脊柱",
        "levator scapulae" to "肩胛提肌",
        "upper back" to "上背",
        "lower back" to "下背",
        "cardiovascular system" to "心肺系统"
    )

    private val mapMuscleGroup: Map<String, String> = mapOf(
        "abdominals" to "腹肌",
        "ankle stabilizers" to "踝关节稳定肌",
        "ankles" to "踝部",
        "biceps" to "肱二头肌",
        "calves" to "小腿肌",
        "chest" to "胸",
        "core" to "核心",
        "deltoids" to "三角肌",
        "forearms" to "前臂",
        "glutes" to "臀大肌",
        "hamstrings" to "腘绳肌",
        "hands" to "手部",
        "hip flexors" to "髋屈肌",
        "latissimus dorsi" to "背阔肌",
        "lats" to "背阔肌",
        "lower back" to "下背",
        "obliques" to "腹斜肌",
        "quadriceps" to "股四头肌",
        "rhomboids" to "菱形肌",
        "rotator cuff" to "肩袖",
        "shoulders" to "肩",
        "soleus" to "比目鱼肌",
        "trapezius" to "斜方肌",
        "traps" to "斜方肌",
        "triceps" to "肱三头肌",
        "upper back" to "上背",
        "wrist extensors" to "腕伸肌",
        "wrist flexors" to "腕屈肌",
        "wrists" to "腕部"
    )
}
