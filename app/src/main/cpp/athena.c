/*
    This file is part of NetGuard.

    NetGuard is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NetGuard is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NetGuard.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2015-2024 by Marcel Bokhorst (M66B)
*/

#include "athena.h"

int loglevel = ANDROID_LOG_WARN;



jclass clsPacket;
jclass clsRR;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) return -1;

    struct rlimit rlim;

    if (!getrlimit(RLIMIT_NOFILE, &rlim)) {
        rlim.rlim_cur = rlim.rlim_max;
        setrlimit(RLIMIT_NOFILE, &rlim);
    }

    return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) == JNI_OK) {
        (*env)->DeleteGlobalRef(env, clsPacket);
        (*env)->DeleteGlobalRef(env, clsRR);
    }
}

JNIEXPORT jlong JNICALL Java_com_kin_athena_service_vpn_service_TunnelManager_jni_1init(JNIEnv *env, jobject instance, jint sdk) {
    struct context *ctx = ng_calloc(1, sizeof(struct context), "init");
    ctx->sdk = sdk;
    if (pthread_mutex_init(&ctx->lock, NULL)) log_android(ANDROID_LOG_ERROR, "pthread_mutex_init failed");
    if (pipe(ctx->pipefds)) log_android(ANDROID_LOG_ERROR, "Create pipe error %d: %s", errno, strerror(errno));
    return (jlong) ctx;
}

JNIEXPORT void JNICALL Java_com_kin_athena_service_vpn_service_TunnelManager_jni_1start(JNIEnv *env, jobject instance, jlong context, jint loglevel_) {
struct context *ctx = (struct context *) context;
loglevel = loglevel_;
ctx->stopping = 0;
}

JNIEXPORT void JNICALL Java_com_kin_athena_service_vpn_service_TunnelManager_jni_1run(JNIEnv *env, jobject instance, jlong context, jint tun, jboolean fwd53, jint rcode) {
struct context *ctx = (struct context *) context;
struct arguments *args = ng_malloc(sizeof(struct arguments), "arguments");
args->env = env;
args->instance = instance;
args->tun = tun;
args->fwd53 = fwd53;
args->rcode = rcode;
args->ctx = ctx;
handle_events(args);
}

JNIEXPORT void JNICALL Java_com_kin_athena_service_vpn_service_TunnelManager_jni_1stop(JNIEnv *env, jobject instance, jlong context) {
struct context *ctx = (struct context *) context;
ctx->stopping = 1;
write(ctx->pipefds[1], "w", 1);
}

JNIEXPORT void JNICALL Java_com_kin_athena_service_vpn_service_TunnelManager_jni_1done(JNIEnv *env, jobject instance, jlong context) {
    if (context == 0) return;
    
    struct context *ctx = (struct context *) context;
    if (ctx == NULL) return;
    
    clear(ctx);
    
    // Only destroy mutex if it was initialized
    if (pthread_mutex_destroy(&ctx->lock) != 0) {
        // Mutex was already destroyed or not initialized, continue cleanup
    }
    
    if (ctx->pipefds[0] >= 0) close(ctx->pipefds[0]);
    if (ctx->pipefds[1] >= 0) close(ctx->pipefds[1]);
    ng_free(ctx, __FILE__, __LINE__);
}

JNIEXPORT jstring JNICALL Java_com_kin_athena_service_vpn_service_TunnelManager_jni_1getprop(JNIEnv *env, jclass type, jstring name_) {
    const char *name = (*env)->GetStringUTFChars(env, name_, 0);
    char value[PROP_VALUE_MAX + 1] = "";
    __system_property_get(name, value);
    (*env)->ReleaseStringUTFChars(env, name_, name);
    return (*env)->NewStringUTF(env, value);
}

JNIEXPORT jint JNICALL
Java_com_kin_athena_service_vpn_service_TunnelManager_jni_1get_1mtu(JNIEnv *env, jobject instance) {
    return get_mtu();
}

JNIEXPORT void JNICALL Java_com_kin_athena_service_vpn_service_TunnelManager_jni_1clear_1sessions(JNIEnv *env, jobject instance, jlong context) {
    if (context == 0) return;
    
    struct context *ctx = (struct context *) context;
    if (ctx == NULL) return;
    
    // Lock the context to prevent race conditions during session clearing
    if (pthread_mutex_lock(&ctx->lock) != 0) {
        log_android(ANDROID_LOG_ERROR, "Failed to lock context for session clearing");
        return;
    }
    
    log_android(ANDROID_LOG_INFO, "Clearing all active sessions");
    
    // Clear all sessions
    clear(ctx);
    
    log_android(ANDROID_LOG_INFO, "All sessions cleared successfully");
    
    // Unlock the context
    if (pthread_mutex_unlock(&ctx->lock) != 0) {
        log_android(ANDROID_LOG_ERROR, "Failed to unlock context after session clearing");
    }
}

void ng_delete_alloc(void *ptr, const char *file, int line) {
#ifdef PROFILE_MEMORY
    if (ptr == NULL)
        return;

    if (pthread_mutex_lock(alock))
        log_android(ANDROID_LOG_ERROR, "pthread_mutex_lock failed");

    int found = 0;
    for (int c = 0; c < allocs; c++)
        if (alloc[c].ptr == ptr) {
            found = 1;
            alloc[c].tag = "[free]";
            alloc[c].ptr = NULL;
            break;
        }

    if (found == 1)
        balance--;

    log_android(found ? ANDROID_LOG_DEBUG : ANDROID_LOG_ERROR,
                "alloc/free balance %d records %d found %d", balance, allocs, found);
    if (found == 0)
        log_android(ANDROID_LOG_ERROR, "Not found at %s:%d", file, line);

    if (pthread_mutex_unlock(alock))
        log_android(ANDROID_LOG_ERROR, "pthread_mutex_unlock failed");
#endif
}

void log_packet_hex(const struct arguments *args, const uint8_t *data, size_t length, const char *direction) {
    if (args == NULL || args->env == NULL || args->instance == NULL || data == NULL) {
        return;
    }
    
    JNIEnv *env = args->env;
    
    jclass cls = (*env)->GetObjectClass(env, args->instance);
    if (cls == NULL) return;
    
    jmethodID methodID = (*env)->GetMethodID(env, cls, "onPacketReceived", "([BILjava/lang/String;)V");
    if (methodID == NULL) {
        (*env)->DeleteLocalRef(env, cls);
        return;
    }
    
    jbyteArray dataArray = (*env)->NewByteArray(env, (jsize)length);
    if (dataArray == NULL) {
        (*env)->DeleteLocalRef(env, cls);
        return;
    }
    
    (*env)->SetByteArrayRegion(env, dataArray, 0, (jsize)length, (const jbyte*)data);
    
    jstring directionStr = (*env)->NewStringUTF(env, direction);
    if (directionStr == NULL) {
        (*env)->DeleteLocalRef(env, dataArray);
        (*env)->DeleteLocalRef(env, cls);
        return;
    }
    
    (*env)->CallVoidMethod(env, args->instance, methodID, dataArray, (jint)length, directionStr);
    
    (*env)->DeleteLocalRef(env, dataArray);
    (*env)->DeleteLocalRef(env, directionStr);
    (*env)->DeleteLocalRef(env, cls);
}

jboolean filter_tcp_packet(const struct arguments *args, const uint8_t *data, size_t length, const char *direction) {
    if (args == NULL || args->env == NULL || args->instance == NULL || data == NULL) {
        return JNI_TRUE; // Allow packet if arguments are invalid
    }
    
    JNIEnv *env = args->env;
    
    jclass cls = (*env)->GetObjectClass(env, args->instance);
    if (cls == NULL) return JNI_TRUE;
    
    jmethodID methodID = (*env)->GetMethodID(env, cls, "onTcpPacketReceived", "([BILjava/lang/String;)Z");
    if (methodID == NULL) {
        (*env)->DeleteLocalRef(env, cls);
        return JNI_TRUE;
    }
    
    jbyteArray dataArray = (*env)->NewByteArray(env, (jsize)length);
    if (dataArray == NULL) {
        (*env)->DeleteLocalRef(env, cls);
        return JNI_TRUE;
    }
    
    (*env)->SetByteArrayRegion(env, dataArray, 0, (jsize)length, (const jbyte*)data);
    
    jstring directionStr = (*env)->NewStringUTF(env, direction);
    if (directionStr == NULL) {
        (*env)->DeleteLocalRef(env, dataArray);
        (*env)->DeleteLocalRef(env, cls);
        return JNI_TRUE;
    }
    
    jboolean result = (*env)->CallBooleanMethod(env, args->instance, methodID, dataArray, (jint)length, directionStr);
    
    (*env)->DeleteLocalRef(env, dataArray);
    (*env)->DeleteLocalRef(env, directionStr);
    (*env)->DeleteLocalRef(env, cls);
    
    return result;
}

jboolean filter_udp_packet(const struct arguments *args, const uint8_t *data, size_t length, const char *direction) {
    if (args == NULL || args->env == NULL || args->instance == NULL || data == NULL) {
        return JNI_TRUE; // Allow packet if arguments are invalid
    }
    
    JNIEnv *env = args->env;
    
    jclass cls = (*env)->GetObjectClass(env, args->instance);
    if (cls == NULL) return JNI_TRUE;
    
    jmethodID methodID = (*env)->GetMethodID(env, cls, "onUdpPacketReceived", "([BILjava/lang/String;)Z");
    if (methodID == NULL) {
        (*env)->DeleteLocalRef(env, cls);
        return JNI_TRUE;
    }
    
    jbyteArray dataArray = (*env)->NewByteArray(env, (jsize)length);
    if (dataArray == NULL) {
        (*env)->DeleteLocalRef(env, cls);
        return JNI_TRUE;
    }
    
    (*env)->SetByteArrayRegion(env, dataArray, 0, (jsize)length, (const jbyte*)data);
    
    jstring directionStr = (*env)->NewStringUTF(env, direction);
    if (directionStr == NULL) {
        (*env)->DeleteLocalRef(env, dataArray);
        (*env)->DeleteLocalRef(env, cls);
        return JNI_TRUE;
    }
    
    jboolean result = (*env)->CallBooleanMethod(env, args->instance, methodID, dataArray, (jint)length, directionStr);
    
    (*env)->DeleteLocalRef(env, dataArray);
    (*env)->DeleteLocalRef(env, directionStr);
    (*env)->DeleteLocalRef(env, cls);
    
    return result;
}

jboolean filter_icmp_packet(const struct arguments *args, const uint8_t *data, size_t length, const char *direction) {
    if (args == NULL || args->env == NULL || args->instance == NULL || data == NULL) {
        return JNI_TRUE; // Allow packet if arguments are invalid
    }
    
    JNIEnv *env = args->env;
    
    jclass cls = (*env)->GetObjectClass(env, args->instance);
    if (cls == NULL) return JNI_TRUE;
    
    jmethodID methodID = (*env)->GetMethodID(env, cls, "onIcmpPacketReceived", "([BILjava/lang/String;)Z");
    if (methodID == NULL) {
        (*env)->DeleteLocalRef(env, cls);
        return JNI_TRUE;
    }
    
    jbyteArray dataArray = (*env)->NewByteArray(env, (jsize)length);
    if (dataArray == NULL) {
        (*env)->DeleteLocalRef(env, cls);
        return JNI_TRUE;
    }
    
    (*env)->SetByteArrayRegion(env, dataArray, 0, (jsize)length, (const jbyte*)data);
    
    jstring directionStr = (*env)->NewStringUTF(env, direction);
    if (directionStr == NULL) {
        (*env)->DeleteLocalRef(env, dataArray);
        (*env)->DeleteLocalRef(env, cls);
        return JNI_TRUE;
    }
    
    jboolean result = (*env)->CallBooleanMethod(env, args->instance, methodID, dataArray, (jint)length, directionStr);
    
    (*env)->DeleteLocalRef(env, dataArray);
    (*env)->DeleteLocalRef(env, directionStr);
    (*env)->DeleteLocalRef(env, cls);
    
    return result;
}