#include <android_native_app_glue.h>

#include "include/native_debug.h"
#include "include/pio/gpio.h"
#include "include/pio/peripheral_manager_client.h"

void android_main(android_app* app) {
    app_dummy(); // prevent native-app-glue to be stripped.

    const char* BUTTON_GPIO = "BCM22";
    APeripheralManagerClient* client = APeripheralManagerClient_new();
    ASSERT(client, "failed to open peripheral manager client");
    AGpio* gpio;
    APeripheralManagerClient_openGpio(client, BUTTON_GPIO, &gpio);
    ASSERT(gpio, "failed to open GPIO: %s", BUTTON_GPIO);
    int setDirectionResult = AGpio_setDirection(gpio, AGPIO_DIRECTION_IN);
    ASSERT(setDirectionResult == 0, "failed to set direction for GPIO: %s", BUTTON_GPIO);
    int setEdgeTriggerResult = AGpio_setEdgeTriggerType(gpio, AGPIO_EDGE_FALLING);
    ASSERT(setEdgeTriggerResult == 0, "failed to edge trigger for GPIO: %s", BUTTON_GPIO);
    int fd;
    int getPollingFdResult = AGpio_getPollingFd(gpio, &fd);
    ASSERT(getPollingFdResult == 0, "failed to get polling file descriptor for GPIO: %s",
           BUTTON_GPIO);
    ALooper* looper = ALooper_forThread();
    ASSERT(looper, "failed to get looper for the current thread");
    int addFdResult = ALooper_addFd(looper, fd, LOOPER_ID_USER, ALOOPER_EVENT_INPUT, NULL, NULL);
    ASSERT(addFdResult > 0, "failed to add file description to looper");

    while (!app->destroyRequested) {
        android_poll_source* source;
        // wait indefinitly for an interrupt or a lifecycle event.
        int pollResult = ALooper_pollOnce(-1, NULL, NULL, (void**)&source);
        if (pollResult >= 0) {
            if (source != NULL) {
                // forward event to native-app-glue to handle lifecycle and input event
                // and update `app` state.
                source->process(app, source);
            }
            if (pollResult == LOOPER_ID_USER) {
                int ackInterruptResult = AGpio_ackInterruptEvent(fd);
                ASSERT(ackInterruptResult == 0, "failed to ack interrupt");
                LOGI("GPIO changed: button pressed");
            }
        }
    }
    ALooper_removeFd(looper, fd);
    AGpio_delete(gpio);
    APeripheralManagerClient_delete(client);
}