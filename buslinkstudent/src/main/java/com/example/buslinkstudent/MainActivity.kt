package com.example.buslinkstudent

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buslinkstudent.theme.BusLinkStudentTheme
import com.example.buslinkstudent.theme.MyBlue
import com.example.buslinkstudent.theme.TxtBlack
import com.example.buslinkstudent.theme.UberFontFamily
import com.example.buslinkstudent.ui.TimeList
import com.example.common.util.extensions.calculateDistance
import com.example.common.util.extensions.capitalizeFirst
import com.example.common.util.extensions.findClosest
import com.example.common.util.extensions.optimizeRoute
import com.example.common.util.extensions.stringToListStops
import com.example.common.util.readFromWebSocket
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.DefaultSettingsProvider
import com.mapbox.maps.extension.compose.DefaultSettingsProvider.createDefault2DPuck
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.camera
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.text.DecimalFormat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(MapboxExperimental::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapboxOptions.accessToken = stringResource(id = R.string.mapbox_public_token)
            BusLinkStudentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: MainViewModel = viewModel()) {

    val state = viewModel.state.value
    val scaffoldState = rememberBottomSheetScaffoldState()

    Box {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 136.dp,
            sheetContent = { BottomSheetContent(scaffoldState) },
            sheetContainerColor = Color.Transparent,
            sheetDragHandle = { BottomSheetButtons() },
            sheetShadowElevation = 0.dp
        ) {
            mapItem()
        }
        SplashScreen()
    }
}

@Composable
fun SplashScreen(viewModel: MainViewModel = viewModel()) {

    val state = viewModel.state.value
    val context = LocalContext.current

    viewModel.onEvent(Event.StartTracking(context))

    var showSplashScreen by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(state.buses) {
        delay(2000)
        showSplashScreen = false
        delay(3000)
        state.mapViewportState.transitionToFollowPuckState { state.mapViewportState.idle() }
    }

    AnimatedVisibility(visible = showSplashScreen, exit = fadeOut()) {
        Column(
            Modifier
                .background(Color.White)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_students_foreground),
                contentDescription = null,
                tint = MyBlue,
                modifier = Modifier.size(160.dp)
            )
        }
    }
}

@OptIn(MapboxExperimental::class)
@Composable
fun BottomSheetButtons(viewModel: MainViewModel = viewModel()) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(
            onClick = { viewModel.onEvent(Event.GoToHome) },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.White,
                contentColor = TxtBlack
            )
        ) {
            Icon(imageVector = Icons.Rounded.Home, contentDescription = null)
        }
        IconButton(
            onClick = { viewModel.onEvent(Event.GoToPuck) },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.White,
                contentColor = TxtBlack
            )
        ) {
            Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = null)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.BottomSheetContent(
    scaffoldState: BottomSheetScaffoldState,
    viewModel: MainViewModel = viewModel()
) {
    val state = viewModel.state.value

    Card(
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Spacer(
            modifier = Modifier
                .height(8.dp)
                .background(Color.White)
                .fillMaxWidth()
        )
        LazyRow(
            Modifier
                .background(Color.White)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            item { Spacer(modifier = Modifier.size(24.dp)) }
            items(state.buses) {
                val isSelected = state.selectedBuss?.bus_num == it.bus_num

                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .scale(if (!isSelected) 1f else 1.2f),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) MyBlue else Color.Transparent),
                    onClick = { viewModel.onEvent(Event.SelectItem(it)) }
                ) {

                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .alpha(if (!isSelected) .7f else 1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = it.bus_num.toString() + " ".repeat(
                                    it.bus.toCharArray().size - it.bus_num.toString()
                                        .toCharArray().size + 10
                                ),
                                color = if (!isSelected) Color.Black else Color.White,
                                style = TextStyle(
                                    fontFamily = UberFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = it.color!!,
                                        shape = RoundedCornerShape(100.dp)
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it.bus,
                            color = if (!isSelected) Color.Black else Color.White,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                            )
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.size(24.dp)) }
        }

        Spacer(
            modifier = Modifier
                .height(16.dp)
                .background(Color.White)
                .fillMaxWidth()
        )

        state.selectedBuss?.let {
            AnimatedVisibility(visible = true) {
                Column(
                    Modifier
                        .offset(y = -16.dp)
                        .padding(horizontal = 16.dp)
                        .padding(start = 24.dp)) {

                    val myPoint =
                        Point.fromLngLat(state.location!!.longitude, state.location.latitude)
                    val stopNearMe: Point = findClosest(myPoint, state.selectedBuss.coords)
                    val distance = calculateDistance(myPoint, stopNearMe)
                    Row (verticalAlignment = Alignment.Bottom, modifier = Modifier.offset(x = -16.dp)){
                        Icon(
                            painter = painterResource(id = R.drawable.walking),
                            contentDescription = null,
                            tint = TxtBlack,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = DecimalFormat("#.#").format(distance).toString() ,
                            color = TxtBlack,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 21.sp
                            )
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "km",
                            color = TxtBlack,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp
                            )
                        )
                        Spacer(modifier = Modifier.size(48.dp))
                        Text(
                            text = ((distance / 5) * 60).toInt().toString(),
                            color = TxtBlack,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 21.sp
                            )
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "min",
                            color = TxtBlack,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                    // --
                    Text(
                        modifier = Modifier.offset(x = -16.dp),
                        text = "Stops",
                        color = TxtBlack,
                        style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.heightIn(8.dp))
                    Text(
                        modifier = Modifier.alpha(.9f),
                        text = state.selectedBuss!!.stops.capitalizeFirst().stringToListStops()
                            .joinToString(", "),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = TxtBlack,
                        style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    //--
                    Text(
                        modifier = Modifier.offset(x = -16.dp),
                        text = "Schedule",
                        color = TxtBlack,
                        style = TextStyle(
                            fontFamily = UberFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.heightIn(8.dp))

                    Column(modifier = Modifier.alpha(.9f)) {
                        val (start, stop) = state.selectedBuss?.bus?.capitalizeFirst()?.split(",")
                            ?: listOf()
                        Text(
                            text = "$start, $stop",
                            color = TxtBlack,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        )
                        TimeList(state.selectedBuss!!.from.stringToListStops())
                        Text(
                            text = "$stop, $start",
                            color = TxtBlack,
                            style = TextStyle(
                                fontFamily = UberFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        )
                        TimeList(state.selectedBuss!!.to.stringToListStops())
                    }


                    Spacer(modifier = Modifier.heightIn(8.dp))

                }
            }
        }


    }


}

@OptIn(MapboxExperimental::class)
@Composable
fun mapItem(viewModel: MainViewModel = viewModel()) {

    val state = viewModel.state.value
    val routs = state.buses.map { it.coords }
    val context = LocalContext.current


    MapboxMap(
        Modifier.fillMaxSize(),
        mapViewportState = state.mapViewportState,
        locationComponentSettings = DefaultSettingsProvider.defaultLocationComponentSettings(
            context,
            LocalDensity.current.density
        )
            .toBuilder()
            //.setLocationPuck(createDefault2DPuck(withBearing = true))
            .setLocationPuck(LocationPuck2D(topImage = ImageHolder.from(R.drawable.puck)))
            .setPuckBearingEnabled(false)
            .setPuckBearing(PuckBearing.HEADING)
            .setEnabled(true)
            .build()
    ) {
        /*MapEffect {
            it.mapboxMap.loadStyle(Style.LIGHT)
        }*/

        MapEffect(state.selectedBuss) { mapView ->
            state.selectedBuss?.let {
                val polygon = Polygon.fromLngLats(listOf(it.route))
                val cameraPosition =
                    mapView.mapboxMap.cameraForGeometry(
                        polygon,
                        EdgeInsets(50.0, 50.0, 100.0, 50.0),
                    )
                mapView.camera.easeTo(cameraPosition)
            }
        }

        routs.forEachIndexed { i, it ->

            val selectedBusNum = state.selectedBuss?.bus_num
            val alphaValue =
                if (selectedBusNum == null || selectedBusNum == state.buses[i].bus_num) 1f else 0f
            val randomColor = state.buses[i].color!!.copy(alpha = alphaValue).toArgb()

            PolylineAnnotation(
                points = state.buses[i].route,
                lineJoin = LineJoin.ROUND,
                lineColorInt = randomColor,
                lineWidth = 8.0,
            )

            it.forEach { point ->
                CircleAnnotation(
                    point = point,
                    circleRadius = 7.0,
                    circleColorInt = randomColor,
                )
            }
        }

        var myRout by remember {
            mutableStateOf<List<Point>?>(null)
        }

        state.selectedBuss?.coords?.let {
            state.location?.let { it1 ->
                val myPoint = Point.fromLngLat(state.location.longitude, it1.latitude)
                val stopNearMe: Point = findClosest(myPoint, it)
                optimizeRoute(listOf(myPoint, stopNearMe)) {
                    myRout = it
                }
            }

            myRout?.let {
                PolylineAnnotation(
                    points = it,
                    lineJoin = LineJoin.ROUND,
                    lineColorInt = MyBlue.toArgb(),
                    lineOpacity = 0.2,
                    lineWidth = 8.0,
                )
                PolylineAnnotation(
                    points = it,
                    lineJoin = LineJoin.ROUND,
                    lineColorInt = MyBlue.toArgb(),
                    lineWidth = 4.0,
                )
            }

            state.liveBusLoc?.let {
                CircleAnnotation(
                    point = it,
                    circleRadius = 16.0,
                    circleOpacity = 0.2,
                    circleColorInt = state.selectedBuss.color!!.toArgb(),
                )
                CircleAnnotation(
                    point = it,
                    circleRadius = 10.0,
                    circleColorInt = state.selectedBuss.color!!.toArgb(),
                )
                CircleAnnotation(
                    point = it,
                    circleRadius = 4.0,
                    circleColorInt = Color.White.toArgb(),
                )
            }
        }
    }
}

