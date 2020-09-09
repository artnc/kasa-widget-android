package com.chaidarun.kasawidget

import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import org.json.JSONObject

object Api {
  private data class DeviceInfo(val token: String, val device: JSONObject, val state: Int)

  fun getState(email: String, password: String, alias: String) =
    getDeviceInfo(email, password, alias)?.state

  fun toggle(email: String, password: String, alias: String): Int {
    val deviceInfo = getDeviceInfo(email, password, alias) ?: return 0
    val nextState = 1 - deviceInfo.state
    rpc(deviceInfo.token,
      deviceInfo.device,
      "set_relay_state",
      JSONObject(mapOf("state" to nextState)))
    return nextState
  }

  private fun getDeviceInfo(email: String, password: String, alias: String): DeviceInfo? {
    // Get token
    val tokenResponse = request("https://wap.tplinkcloud.com", JSONObject(mapOf(
      "method" to "login",
      "params" to JSONObject(mapOf(
        "appType" to "Kasa_Android",
        "cloudPassword" to password,
        "cloudUserName" to email,
        // Generate a deterministic UUID just in case TP-Link expects it to be reused, e.g. if
        // it represents a distinct API key then using random UUIDs might cause TP-Link's future
        // hypothetical user-accessible API dashboard to be spammed with a million distinct keys
        "terminalUUID" to UUID.nameUUIDFromBytes(email.toByteArray()).toString(),
      )),
    )))
    val token = JSONObject(tokenResponse).getJSONObject("result").getString("token")

    // Get device
    val devicesResponse = request("https://wap.tplinkcloud.com?token=$token",
      JSONObject(mapOf("method" to "getDeviceList")))
    val devices = JSONObject(devicesResponse).getJSONObject("result").getJSONArray("deviceList")
    val device = (0 until devices.length()).map { devices.getJSONObject(it) }
      .find { it.getString("alias") == alias } ?: return null

    return DeviceInfo(token, device, rpc(token, device, "get_sysinfo").getInt("relay_state"))
  }

  private fun rpc(token: String, device: JSONObject, api: String, params: JSONObject? = null) =
    JSONObject(JSONObject(request("${device.getString("appServerUrl")}?token=$token",
      JSONObject(mapOf(
        "method" to "passthrough",
        "params" to JSONObject(mapOf(
          "deviceId" to device.getString("deviceId"),
          "requestData" to JSONObject(mapOf("system" to JSONObject(mapOf(api to (params
            ?: JSONObject()))))).toString(),
        )),
      )))).getJSONObject("result")
      .getString("responseData")).getJSONObject("system")
      .getJSONObject(api)

  private fun request(url: String, body: JSONObject? = null) =
    (URL(url).openConnection() as HttpURLConnection).run {
      if (body != null) {
        requestMethod = "POST"
        setRequestProperty("Content-Type", "application/json")
        outputStream.use {
          val bytes = body.toString().toByteArray()
          it.write(bytes, 0, bytes.size)
        }
      }
      inputStream.bufferedReader().use {
        val response = StringBuffer()
        var line = it.readLine()
        while (line != null) {
          response.append(line)
          line = it.readLine()
        }
        response.toString()
      }
    }
}
