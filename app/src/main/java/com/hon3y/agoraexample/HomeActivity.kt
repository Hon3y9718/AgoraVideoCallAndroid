package com.hon3y.agoraexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas

class HomeActivity : AppCompatActivity() {


    // Fill the App ID of your project generated on Agora Console.
    private val APP_ID = "ef5e4aef5710497db503b1eba6a95e01"
    // Fill the channel name.
    private val CHANNEL = "AgoraTestAppChannel"
    // Fill the temp token generated on Agora Console.
    private val TOKEN = "007eJxTYNA6E+F4f92b8+tbjn08/iQte5+h1NoZHLujwzr7L3+/d11BgSE1zTTVJBFImhsamFiapySZGhgnGaYmJZolWpqmGhhuZNJPNgwxSGb5ocLCyACBIL4wg2N6flFiSGpxiWNBgXNGYl5eag4DAwCK0ygl"

    private var mRtcEngine: RtcEngine?= null

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote user joining the channel to get the uid of the user.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                // Call setupRemoteVideo to set the remote video view after getting uid from the onUserJoined callback.
                setupRemoteVideo(uid)
            }
        }
    }

    private val PERMISSION_REQ_ID_RECORD_AUDIO = 22
    private val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(permission),
                requestCode)
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        var isAudioDisabled = false;

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(
                Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initializeAndJoinChannel()
            // get reference to button
            val switchCameraButton = findViewById<Button>(R.id.switchCamera)

            val muteButton = findViewById<Button>(R.id.mute)

            val endCallButton = findViewById<Button>(R.id.endCall)
            // set on-click listener
            switchCameraButton.setOnClickListener {
                mRtcEngine?.switchCamera()
//                mRtcEngine?.disableVideo()
            }

            muteButton.setOnClickListener {
                if (isAudioDisabled){
                    mRtcEngine?.enableAudio()
                    muteButton.setText("UnMute")
                }
                else{
                    mRtcEngine?.disableAudio()
                    muteButton.setText("Mute")
                }
                isAudioDisabled = !isAudioDisabled;
             }

            endCallButton.setOnClickListener {
                mRtcEngine?.leaveChannel()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
    }

    private fun initializeAndJoinChannel() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, APP_ID, mRtcEventHandler)
        } catch (e: Exception) {

        }

        // By default, video is disabled, and you need to call enableVideo to start a video stream.
        mRtcEngine!!.enableVideo()

        val localContainer = findViewById(R.id.local_video_view_container) as FrameLayout
        // Call CreateRendererView to create a SurfaceView object and add it as a child to the FrameLayout.
        val localFrame = RtcEngine.CreateRendererView(baseContext)
        localContainer.addView(localFrame)
        // Pass the SurfaceView object to Agora so that it renders the local video.
        mRtcEngine!!.setupLocalVideo(VideoCanvas(localFrame, VideoCanvas.RENDER_MODE_FIT, 0))

        // Join the channel with a token.
        mRtcEngine!!.joinChannel(TOKEN, CHANNEL, "", 0)
    }


    private fun setupRemoteVideo(uid: Int) {
        val remoteContainer = findViewById(R.id.remote_video_view_container) as FrameLayout

        val remoteFrame = RtcEngine.CreateRendererView(baseContext)
        remoteFrame.setZOrderMediaOverlay(true)
        remoteContainer.addView(remoteFrame)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(remoteFrame, VideoCanvas.RENDER_MODE_FIT, uid))
    }
}