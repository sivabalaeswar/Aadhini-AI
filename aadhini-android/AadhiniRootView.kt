package ai.aadhini.app

import android.content.Context
import android.graphics.*
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import ai.aadhini.app.ui.environment.DeviceId
import ai.aadhini.app.ui.navigation.AadhiniUiController
import ai.aadhini.app.ui.navigation.AppRoute
import kotlin.math.min
import kotlin.math.sin

/** Presentation host. UI state is delegated to the presentation controller. */
class AadhiniRootView(
    context: Context,
    private val controller: AadhiniUiController = AadhiniUiController()
) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val handler = Handler(Looper.getMainLooper())
    private val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55)
    private var bootProgress = 0f
    private var bootStep = 0
    private var pulse = 0f
    private val bootItems = arrayOf("Memory", "Context", "Decision", "Platform")

    init {
        isFocusable = true
        post(frameLoop)
        post(bootLoop)
    }

    private val frameLoop = object : Runnable {
        override fun run() { pulse += .035f; invalidate(); postDelayed(this, 33L) }
    }

    private val bootLoop = object : Runnable {
        override fun run() {
            if (controller.state.bootComplete) return
            bootProgress = (bootProgress + .035f).coerceAtMost(1f)
            val step = (bootProgress * bootItems.size).toInt().coerceAtMost(bootItems.size)
            if (step > bootStep) { bootStep = step; if (!controller.state.muted) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 55) }
            if (bootProgress >= 1f) {
                if (!controller.state.muted) tone.startTone(ToneGenerator.TONE_PROP_ACK, 90)
                controller.completeBoot()
                invalidate()
            } else { invalidate(); postDelayed(this, 85L) }
        }
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacksAndMessages(null)
        tone.release()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.rgb(5, 6, 8))
        drawAurora(canvas)
        if (!controller.state.bootComplete) drawBoot(canvas)
        else when (controller.state.route) {
            AppRoute.HOME -> drawHome(canvas)
            AppRoute.CONVERSATION -> drawConversation(canvas)
            AppRoute.SETTINGS -> drawSettings(canvas)
            AppRoute.DEVICE -> drawDeviceScreen(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        val x = event.x; val y = event.y; val w = width.toFloat(); val h = height.toFloat()
        if (!controller.state.bootComplete) {
            if (x > w - 70 && y < 70) controller.setMuted(!controller.state.muted)
            invalidate(); return true
        }
        when (controller.state.route) {
            AppRoute.HOME -> when {
                y < 70 && x < 125 -> controller.openDevice()
                y < 70 && x > w - 115 -> controller.openConversation()
                y in 240f..340f -> controller.openConversation()
                y in 340f..440f -> controller.openDevice()
                y in 440f..540f -> controller.openSettings()
            }
            AppRoute.CONVERSATION -> if (y < 90 && x < 80) controller.goHome()
            AppRoute.SETTINGS -> if (y < 90 && x < 80) controller.goHome()
            AppRoute.DEVICE -> {
                if (y < 90 && x < 80) controller.goHome()
                else if (y in 140f..220f) controller.selectDevice(DeviceId.MOTO, true)
                else if (y in 220f..300f) controller.selectDevice(DeviceId.OPPO, true)
                else if (y in 300f..380f) controller.selectDevice(DeviceId.BIKE_E, true)
                else if (y in 380f..460f) controller.selectDevice(DeviceId.TV, true)
            }
        }
        invalidate(); return true
    }

    private fun drawAurora(c: Canvas) {
        val r = min(width, height) * .48f
        val g = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = RadialGradient(width*.52f, height*.45f, r, intArrayOf(0x333D52FF,0x181F2A78,0x00050608), floatArrayOf(0f,.45f,1f), Shader.TileMode.CLAMP) }
        c.drawCircle(width*.52f, height*.45f, r, g)
    }

    private fun drawBoot(c: Canvas) {
        val w=width.toFloat(); val h=height.toFloat(); val cx=w/2; val cy=h*.34f
        logo(c,cx,cy,min(w,h)*.095f,true)
        text(c,"A A D H I N I",cx,cy+h*.10f,20f,0xFFF7F8FF.toInt(),true)
        text(c,"Restoring your world...",cx,cy+h*.17f,16f,0xB8D7DCEF.toInt(),true)
        val top=h*.56f; val row=h*.075f
        bootItems.forEachIndexed { i,label ->
            text(c,label,w*.14f,top+row*i,16f,0xFFF5F7FF.toInt(),false)
            text(c,if(i<bootStep)"✓ Ready" else "Loading",w*.86f,top+row*i,16f,if(i<bootStep)0xFF18E39A.toInt() else 0x85AEB7C8.toInt(),true,true)
        }
        val l=w*.16f; val r=w*.84f; val by=top+row*bootItems.size+h*.035f
        round(c,RectF(l,by,r,by+5),3f,0x281F2A44); round(c,RectF(l,by,l+(r-l)*bootProgress,by+5),3f,0xFF6678FF.toInt())
        text(c,"Continue where we left off.",cx,h*.88f,15f,0x85AEB7C8.toInt(),true)
        mute(c,w-30f,30f)
    }

    private fun drawHome(c: Canvas) {
        val w=width.toFloat(); val h=height.toFloat(); val p=20f
        deviceChip(c,p,30f); avatar(c,w-55f,30f)
        logo(c,p+28f,92f,27f,false); text(c,"AADHINI",p+66f,96f,18f,0xFFF5F7FF.toInt(),false)
        text(c,"Good day, Chief.",p+66f,120f,14f,0xB8D7DCEF.toInt(),false)
        glass(c,RectF(p,148f,w-p,220f),22f); text(c,"TODAY'S BRIEF",p+18f,175f,12f,0xB8D7DCEF.toInt(),false); text(c,"You're all caught up.",p+18f,202f,17f,0xFFF5F7FF.toInt(),false)
        val top=240f
        card(c,p,top,w-p,318f,"◉","Conversation","Chat, voice & avatar",2)
        card(c,p,330f,w-p,408f,"☎","Calls","Quick action",0)
        card(c,p,420f,w-p,498f,"◈","Device","${controller.state.device.device.name.replace("BIKE_E","BIK-E")}",1)
        card(c,p,510f,w-p,588f,"▣","Settings","Preferences",0)
        if(h>650) text(c,"Tap a card to open its module",w/2,h-28f,12f,0x85AEB7C8.toInt(),true)
    }

    private fun drawConversation(c: Canvas) {
        val w=width.toFloat(); val h=height.toFloat(); back(c,28f,34f); logo(c,90f,34f,21f,false)
        text(c,"AADHINI",122f,39f,16f,0xFFF5F7FF.toInt(),false); text(c,"Ready to listen",122f,59f,11f,0xFF18E39A.toInt(),false); avatar(c,w-34f,34f)
        text(c,"How can I help, Chief?",18f,h*.30f,25f,0xFFF5F7FF.toInt(),false); text(c,"Text or voice — both work here.",18f,h*.30f+30f,14f,0xB8D7DCEF.toInt(),false)
        glass(c,RectF(18f,h-78f,w-18f,h-18f),28f); text(c,"Message Aadhini...",38f,h-42f,14f,0x85AEB7C8.toInt(),false); text(c,"●",w-50f,h-40f,18f,0xFF61E7FF.toInt(),true)
    }

    private fun drawSettings(c: Canvas) {
        back(c,28f,34f); text(c,"Settings",70f,40f,20f,0xFFF5F7FF.toInt(),false)
        card(c,18f,100f,width-18f,178f,"◉","Chat preferences","Conversation behaviour",0)
        card(c,18f,190f,width-18f,268f,"✦","Avatar preferences","Presence & avatar behaviour",0)
        card(c,18f,280f,width-18f,358f,"⌁","Notifications","Priority and privacy",0)
    }

    private fun drawDeviceScreen(c: Canvas) {
        back(c,28f,34f); text(c,"Devices",70f,40f,20f,0xFFF5F7FF.toInt(),false)
        val ids=DeviceId.values(); ids.forEachIndexed { i,d -> card(c,18f,100f+i*82f,width-18f,170f+i*82f,"◈",d.name.replace("BIKE_E","BIK-E"),if(d==controller.state.device.device)"Selected" else "Tap to switch",if(d==controller.state.device.device)1 else 0) }
    }

    private fun card(c:Canvas,l:Float,t:Float,r:Float,b:Float,icon:String,title:String,sub:String,badge:Int){glass(c,RectF(l,t,r,b),22f);text(c,icon,l+20f,t+35f,23f,0xFF9A72FF.toInt(),false);text(c,title,l+58f,t+32f,16f,0xFFF5F7FF.toInt(),false);text(c,sub,l+58f,t+55f,12f,0xB8D7DCEF.toInt(),false);if(badge>0){paint.color=0xFF6678FF.toInt();c.drawCircle(r-24f,t+25f,12f,paint);text(c,badge.toString(),r-24f,t+29f,11f,Color.WHITE,true)}}
    private fun glass(c:Canvas,r:RectF,rad:Float){round(c,r,rad,0x190B0D12);stroke.color=0x2AFFFFFF;stroke.strokeWidth=1f;c.drawRoundRect(r,rad,rad,stroke)}
    private fun round(c:Canvas,r:RectF,rad:Float,color:Int){paint.shader=null;paint.color=color;c.drawRoundRect(r,rad,rad,paint)}
    private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,color:Int,center:Boolean,right:Boolean=false){paint.shader=null;paint.color=color;paint.textSize=size;paint.typeface=Typeface.create("sans",if(center)Typeface.BOLD else Typeface.NORMAL);paint.textAlign=if(right)Paint.Align.RIGHT else if(center)Paint.Align.CENTER else Paint.Align.LEFT;c.drawText(s,x,y,paint)}
    private fun logo(c:Canvas,cx:Float,cy:Float,size:Float,animated:Boolean){val p=Path();p.moveTo(cx-size*.72f,cy+size*.72f);p.lineTo(cx,cy-size*.82f);p.lineTo(cx+size*.72f,cy+size*.72f);p.lineTo(cx+size*.43f,cy+size*.72f);p.lineTo(cx,cy-size*.15f);p.lineTo(cx-size*.43f,cy+size*.72f);p.close();val f=Paint(Paint.ANTI_ALIAS_FLAG).apply{shader=LinearGradient(cx,cy-size,cx,cy+size,0xFF6678FF.toInt(),0xFF9A72FF.toInt(),Shader.TileMode.CLAMP)};c.drawPath(p,f);if(animated){paint.color=0x556678FF;c.drawCircle(cx,cy,size*(1.18f+.08f*sin(pulse*2)),paint)}}
    private fun avatar(c:Canvas,cx:Float,cy:Float){paint.color=0xFF11141C.toInt();c.drawCircle(cx,cy,25f,paint);text(c,"A",cx,cy+6f,18f,0xFF9A72FF.toInt(),true);paint.color=0xFF18E39A.toInt();c.drawCircle(cx+17f,cy+17f,5f,paint)}
    private fun deviceChip(c:Canvas,x:Float,y:Float){glass(c,RectF(x,y-18,x+82,y+18),18f);text(c,controller.state.device.device.name.replace("BIKE_E","BIK-E"),x+41,y+5,10f,0xFF61E7FF.toInt(),true)}
    private fun mute(c:Canvas,x:Float,y:Float){paint.color=0xB8F5F7FF.toInt();c.drawCircle(x,y,14f,paint);text(c,if(controller.state.muted)"×" else "•",x,y+5,13f,0xFF050608.toInt(),true)}
    private fun back(c:Canvas,x:Float,y:Float){text(c,"‹",x,y+9,34f,0xFFF5F7FF.toInt(),false)}
}
