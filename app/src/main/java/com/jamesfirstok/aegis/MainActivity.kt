// ... نفس الاستيرادات السابقة مع إضافة:
import com.jamesfirstok.aegis.core.AegisSystemOrchestrator

class MainActivity : ComponentActivity() {
    // 1. حقن المشغل الرئيسي (Orchestrator) الذي يربط AI و C++
    private lateinit var orchestrator: AegisSystemOrchestrator

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // طلب صلاحيات الوصول للراديو والموقع
        if (permissions.all { it.value }) {
            orchestrator.initializeTacticalCore()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orchestrator = AegisSystemOrchestrator(this)
        
        // طلب كافة الصلاحيات التكتيكية
        permissionLauncher.launch(arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WAKE_LOCK
        ))

        setContent {
            AegisTheme {
                Surface(color = Color.Black) {
                    // تمرير Orchestrator للواجهة لإرسال أوامر الضرب
                    TacticalHUD(orchestrator)
                }
            }
        }
    }
}

@Composable
fun TacticalHUD(orchestrator: AegisSystemOrchestrator) {
    // ... الكود السابق للرسم ...

    // 2. إضافة زر "التحييد القسري" (Neutralize)
    Button(
        onClick = { orchestrator.executeManualOverride() }, // استدعاء حقن MAVLink
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Text("FORCE LANDING / JAMMING", color = Color.White, fontWeight = FontWeight.Bold)
    }
}
