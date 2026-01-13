package cat.dam.alaric.carrera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class TipusVehicle(val velocitatMaxima: Int, val imatge: Int) {
    MOTO(3, R.drawable.moto),
    TURISME(4, R.drawable.turisme),
    ESPORTIU(5, R.drawable.esportiu),
    FURGONETA(2, R.drawable.furgo),
    CAMIO(1, R.drawable.camio)
}

data class Vehicle(
    val id: Int,
    val tipus: TipusVehicle,
    var posicio: Int = 0,
    val colorDorsal: Color,
    val nom: String,
    var haArribat: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                CarreraScreen()
            }
        }
    }
}


@Composable
fun CarreraScreen() {
    var vehicles by rememberSaveable { mutableStateOf(emptyList<Vehicle>()) }
    var cursaEnMarxa by rememberSaveable { mutableStateOf(false) }
    var classificacio by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val scope = rememberCoroutineScope()

    val crearVehicles = {
        classificacio = emptyList()
        cursaEnMarxa = false
        vehicles = List(5) { i ->
            val tipus = TipusVehicle.values().random()
            Vehicle(
                id = i + 1,
                tipus = tipus,
                colorDorsal = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 0.7f),
                nom = "${tipus.name.lowercase()}${i + 1}"
            )
        }
    }

    val iniciarCursa = {
        cursaEnMarxa = true
        scope.launch {
            while (cursaEnMarxa && vehicles.any { !it.haArribat }) {
                delay(300)
                val nousVehicles = vehicles.map { v ->
                    if (!v.haArribat) {
                        val nouAvanç = Random.nextInt(1, v.tipus.velocitatMaxima + 1)
                        val novaPos = v.posicio + nouAvanç
                        if (novaPos >= 100) {
                            v.copy(posicio = 100, haArribat = true).also {
                                classificacio = classificacio + it.nom
                            }
                        } else v.copy(posicio = novaPos)
                    } else v
                }
                vehicles = nousVehicles
            }
            cursaEnMarxa = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = crearVehicles) { Text("CREATE") }
            Button(
                onClick = { if (cursaEnMarxa) cursaEnMarxa = false else iniciarCursa() },
                enabled = vehicles.isNotEmpty() && vehicles.any { !it.haArribat }
            ) {
                Text(if (cursaEnMarxa) "PAUSE" else "RUN")
            }
        }

        Spacer(Modifier.height(20.dp))

        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            vehicles.forEach { vehicle ->
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val amplePista = maxWidth

                    Box(Modifier.fillMaxHeight().width(4.dp).background(Color.Red).align(Alignment.CenterEnd))

                    VehicleRow(vehicle, amplePista)
                }
                HorizontalDivider()
            }
        }

        Text(
            text = "Arribada: ${classificacio.joinToString(", ")}",
            modifier = Modifier.padding(top = 8.dp),
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        )
    }
}


@Composable
fun VehicleRow(vehicle: Vehicle, amplePista: Dp) {
    val midaVehicle = 60.dp

    val recorregutMaxim = amplePista - midaVehicle
    val destinacio = recorregutMaxim * (vehicle.posicio / 100f)

    val animPos by animateDpAsState(targetValue = destinacio)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = animPos),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = vehicle.tipus.imatge),
                contentDescription = null,
                modifier = Modifier.size(midaVehicle),
                tint = Color.Unspecified
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(vehicle.colorDorsal),
                contentAlignment = Alignment.Center
            ) {
                Text(vehicle.id.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Text(vehicle.nom, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp))
    }
}