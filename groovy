/**
 *  Rheem Controller
 *  Based on Attic Fan Control by Mryan Li
 *
 *  
 *
 */
definition(
    name: "Rheem Controller",
    namespace: "michaeldbaird",
    author: "Michael Baird",
    description: "Adjust ducting on hybrind water heater based on attic and garage temperatures",
    category: "Green Living",
)

preferences {
    section("Garage") {
        input "outTemp", "capability.temperatureMeasurement", title: "Garage Thermometer", required: true
        input "minTempDiff", "number", title: "Minimum Temperature Difference", required: true
    }

    section("Attic") {
    	input "inTemp", "capability.temperatureMeasurement", title: "Attic", required: true
        input "fan", "capability.switch", title: "Duct Control", required: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    state.fanRunning = false
    state.overrideOn = false

    subscribe(fan, "switch", "switchHandler")
    subscribe(outTemp, "temperature", "checkThings")
    subscribe(inTemp, "temperature", "checkThings")
}

// Main function for Rheem Controller
def checkThings(evt) {
        // Current Temperatures
        def outsideTemp = settings.outTemp.currentTemperature
        def insideTemp = settings.inTemp.currentTemperature    

        // Begin Logic
        def shouldRun = true

        // Check Temperatures
        if(insideTemp < outsideTemp) {
            shouldRun = false
        }

        // Check Temperature Difference
        if (insideTemp - outsideTemp < settings.minTempDiff) {
            shouldRun = false
        }

        // Turn fan on or off
        if(shouldRun) {
            if(settings.fan.currentValue("switch") == 'off') {
                fan.on()
                state.fanRunning = true
            }
        } else if(!shouldRun) {
            if(settings.fan.currentValue("switch") == 'on') {
                fan.off()
                state.fanRunning = false
            }
        }
    }
}


// Fan On/Off handlers
def switchHandler(evt){
    if(evt.value == "on")
        state.fanRunning = true
    else if(evt.value == "off")
        state.fanRunning = false

    checkThings(evt)
}
