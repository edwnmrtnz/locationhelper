
# locationhelper  [![](https://jitpack.io/v/edwnmrtnz/locationhelper.svg)](https://jitpack.io/#edwnmrtnz/locationhelper)
 
FusedLocationProviderClient wrapper using Kotlin Coroutines.

### Benefits
1. Get oneshot coordinates of the current device.
2. Get coordinates as Flow.
3. FragmentScoped, ActivityScoped, ApplicationScoped? No problem.


### Prerequisites
```gradle
allprojects {
    repositories {
	    maven { url 'https://jitpack.io' }
	}
}
```
### Dependency
Add this to your module's build.gradle file (make sure the version matches the JitPack badge above):
```gradle
dependencies {
    implementation 'com.github.edwnmrtnz:locationhelper:Tag'
}
```

### Sample Usage

```
// Try to obtain a viable location within 1 min with 500m radius.
val job = scope.launch {
    val result = withTimeoutOrNull(60_000L) {
        locationHelper.getViableCurrentLocation(500f)
    }
}
```







### TODO
Get continuous location updates with Coroutine Flows.