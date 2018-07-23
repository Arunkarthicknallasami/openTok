package com.tokbox.pictureinpicturesample;

import android.Manifest;
import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

public class SessionActivity extends Activity implements Session.SessionListener {

    public static final String API_KEY = "46146822";
    public static final String TOKEN = "T1==cGFydG5lcl9pZD00NjE0NjgyMiZzaWc9MzEzZWVjZGIzN2NhNzk0YTkyOGI0OGEwZGRkYWY2YjM2MjRiN2ExMjpzZXNzaW9uX2lkPTFfTVg0ME5qRTBOamd5TW41LU1UVXpNVGd5T0RFeE56TXhPWDUyTDJnNGNVTnVaRXRPVmtwV2FqTmpabEZ4UVdFelJ5OS1RWDQmY3JlYXRlX3RpbWU9MTUzMTgyODE2NCZub25jZT0zNjM5MDUmcm9sZT1NT0RFUkFUT1ImZXhwaXJlX3RpbWU9MTUzMjQzMjk2NCZjb25uZWN0aW9uX2RhdGE9bmFtZSUzZEthcnRoaQ==";
    public static final String SESSION_ID = "1_MX40NjE0NjgyMn5-MTUzMTgyODExNzMxOX52L2g4cUNuZEtOVkpWajNjZlFxQWEzRy9-QX4";

    Session session;
    Publisher publisher;
    Subscriber subscriber;
    private static final String TAG = SessionActivity.class.getSimpleName();

    private boolean isPublisher = false;
    DragFrameLayout frame_drag;
    FrameLayout frameFullScreen, framePop;
    public boolean isDragged = false;
    Button btn_pip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Activity Instance: " + this.toString());
        setContentView(R.layout.session_activity);
        frame_drag = findViewById(R.id.frame_drag);
        frameFullScreen = findViewById(R.id.frameFullScreen);
        framePop = findViewById(R.id.framePop);
        btn_pip = findViewById(R.id.btn_pip);
        frame_drag.setDragFrameController(new DragFrameLayout.DragFrameLayoutController() {

            @Override
            public void onDragDrop(boolean captured, View view) {
                 /* Animate the translation of the {@link View}. Note that the translation
                 is being modified, not the elevation. */
                framePop.animate()
                        .translationZ(captured ? 50 : 0)
                        .setDuration(100);
                if (captured) {
                    isDragged = captured;
                }
            }

            @Override
            public void onclick(float x, float y) {
                if (isPointInsideView(x, y, framePop)) {
                    if (!isPublisher) {
                        isPublisher = true;
                        frameFullScreen.removeAllViews();
                        framePop.removeAllViews();
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) getResources().getDimension(R.dimen._90sdp), (int) getResources().getDimension(R.dimen._120sdp));
                        params.setMargins(framePop.getLeft(), framePop.getTop(), framePop.getRight(), framePop.getBottom());
                        frame_drag.clearDragView();
                        frame_drag.removeAllViews();
                        if (subscriber != null) {
                            framePop.addView(subscriber.getView());
                        }
                        if (publisher != null) {
                            frameFullScreen.addView(publisher.getView());
                            if (publisher.getView() instanceof GLSurfaceView) {
                                ((GLSurfaceView) publisher.getView()).setZOrderOnTop(false);
                            }
                        }
                        frame_drag.addView(frameFullScreen);
                        frame_drag.addView(framePop);
                        // frame_drag.addView(btn_pip);
                        framePop.setLayoutParams(params);
                        frame_drag.addDragView(framePop);

                    } else {
                        isPublisher = false;
                        frameFullScreen.removeAllViews();
                        framePop.removeAllViews();
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) getResources().getDimension(R.dimen._90sdp), (int) getResources().getDimension(R.dimen._120sdp));
                        params.setMargins(framePop.getLeft(), framePop.getTop(), framePop.getRight(), framePop.getBottom());
                        frame_drag.clearDragView();
                        frame_drag.removeAllViews();
                        if (publisher != null) {
                            framePop.addView(publisher.getView());
                            if (publisher.getView() instanceof GLSurfaceView) {
                                ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
                            }
                        }
                        if (subscriber != null) {
                            frameFullScreen.addView(subscriber.getView());
                        }
                        frame_drag.addView(frameFullScreen);
                        frame_drag.addView(framePop);
//                        frame_drag.addView(btn_pip);
                        framePop.setLayoutParams(params);
                        frame_drag.addDragView(framePop);
                    }
                }

            }
        });
        frame_drag.addDragView(framePop);
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1000);
    }

    private boolean isPointInsideView(float x, float y, View view) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        // point is inside view bounds
        return ((x > viewX && x < (viewX + view.getWidth())) &&
                (y > viewY && y < (viewY + view.getHeight())));
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (publisher != null) {
            if (isInPictureInPictureMode) {
                framePop.setVisibility(View.GONE);
                publisher.getView().setVisibility(View.GONE);
                getActionBar().hide();
            } else {
                framePop.setVisibility(View.VISIBLE);
                publisher.getView().setVisibility(View.VISIBLE);
                if (publisher.getView() instanceof GLSurfaceView) {
                    ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
                }
                getActionBar().show();
            }
        } else {
            Toast.makeText(SessionActivity.this, "Cannot enter PictureInPicture Mode", Toast.LENGTH_SHORT).show();
        }

    }

    public void pipActivity() {
        PictureInPictureParams params = new PictureInPictureParams.Builder()
                .setAspectRatio(new Rational(9, 16)) // Portrait Aspect Ratio
                .build();
        enterPictureInPictureMode(params);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        if (session == null) {
            session = new Session.Builder(getApplicationContext(), API_KEY, SESSION_ID)
                    .build();
        }
        session.setSessionListener(this);
        session.connect(TOKEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isInPictureInPictureMode()) {
            if (session != null) {
                session.onPause();
                if (isPublisher) {
                    frameFullScreen.removeAllViews();
                    framePop.removeAllViews();
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) getResources().getDimension(R.dimen._90sdp), (int) getResources().getDimension(R.dimen._120sdp));
                    params.setMargins(framePop.getLeft(), framePop.getTop(), framePop.getRight(), framePop.getBottom());
                    frame_drag.clearDragView();
                    frame_drag.removeAllViews();
                    frame_drag.addView(frameFullScreen);
                    frame_drag.addView(framePop);
//                    frame_drag.addView(btn_pip);
                    framePop.setLayoutParams(params);
                    frame_drag.addDragView(framePop);
                } else {
                    frameFullScreen.removeAllViews();
                    framePop.removeAllViews();
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) getResources().getDimension(R.dimen._90sdp), (int) getResources().getDimension(R.dimen._120sdp));
                    params.setMargins(framePop.getLeft(), framePop.getTop(), framePop.getRight(), framePop.getBottom());
                    frame_drag.clearDragView();
                    frame_drag.removeAllViews();
                    frame_drag.addView(frameFullScreen);
                    frame_drag.addView(framePop);
//                    frame_drag.addView(btn_pip);
                    framePop.setLayoutParams(params);
                    frame_drag.addDragView(framePop);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isInPictureInPictureMode()) {
            if (session != null) {
                session.onResume();
                if (isPublisher) {
                    frameFullScreen.removeAllViews();
                    framePop.removeAllViews();
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) getResources().getDimension(R.dimen._90sdp), (int) getResources().getDimension(R.dimen._120sdp));
                    params.setMargins(framePop.getLeft(), framePop.getTop(), framePop.getRight(), framePop.getBottom());
                    frame_drag.clearDragView();
                    frame_drag.removeAllViews();
                    if (subscriber != null) {
                        framePop.addView(subscriber.getView());
                    }
                    if (publisher != null) {
                        frameFullScreen.addView(publisher.getView());
                        if (publisher.getView() instanceof GLSurfaceView) {
                            ((GLSurfaceView) publisher.getView()).setZOrderOnTop(false);
                        }
                    }
                    frame_drag.addView(frameFullScreen);
                    frame_drag.addView(framePop);
//                    frame_drag.addView(btn_pip);
                    framePop.setLayoutParams(params);
                    frame_drag.addDragView(framePop);
                } else {
                    frameFullScreen.removeAllViews();
                    framePop.removeAllViews();
                    FrameLayout.LayoutParams params;
                    if (isDragged) {
                        params = new FrameLayout.LayoutParams((int) getResources().getDimension(R.dimen._90sdp), (int) getResources().getDimension(R.dimen._120sdp));
                        params.setMargins(framePop.getLeft(), framePop.getTop(), framePop.getRight(), framePop.getBottom());
                    } else {
                        params = new FrameLayout.LayoutParams((int) getResources().getDimension(R.dimen._90sdp), (int) getResources().getDimension(R.dimen._120sdp));
                        params.gravity = Gravity.TOP | Gravity.END;
                        params.setMargins(0, (int) getResources().getDimension(R.dimen._16sdp), (int) getResources().getDimension(R.dimen._16sdp), 0);
                        framePop.setLayoutParams(params);
                    }
                    frame_drag.clearDragView();
                    frame_drag.removeAllViews();
                    if (publisher != null) {
                        framePop.addView(publisher.getView());
                        if (publisher.getView() instanceof GLSurfaceView) {
                            ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
                        }
                    }
                    if (subscriber != null) {
                        frameFullScreen.addView(subscriber.getView());
                    }
                    frame_drag.addView(frameFullScreen);
                    frame_drag.addView(framePop);
//                    frame_drag.addView(btn_pip);
                    framePop.setLayoutParams(params);
                    frame_drag.addDragView(framePop);
                }
            }
        }
    }

    // Session Listener
    @Override
    public void onConnected(Session session) {
        Log.d(TAG, "Session connected");

        if (publisher == null) {
            publisher = new Publisher.Builder(getApplicationContext()).build();
            session.publish(publisher);
            if (isPublisher) {
                frameFullScreen.addView(publisher.getView());
                if (publisher.getView() instanceof GLSurfaceView) {
                    ((GLSurfaceView) publisher.getView()).setZOrderOnTop(false);
                }

            } else {
                if (publisher != null) {
                    framePop.addView(publisher.getView());
                    if (publisher.getView() instanceof GLSurfaceView) {
                        ((GLSurfaceView) publisher.getView()).setZOrderOnTop(true);
                    }
                }
            }
        }

    }

    @Override
    public void onDisconnected(Session session) {
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        if (subscriber == null) {
            subscriber = new Subscriber.Builder(getApplicationContext(), stream).build();
            session.subscribe(subscriber);
            if (isPublisher) {
                framePop.addView(subscriber.getView());
            } else {
                frameFullScreen.addView(subscriber.getView());
            }
        } else {
            Log.d(TAG, "This sample supports just one subscriber");
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        if (isPublisher) {
            framePop.removeAllViews();
        } else {
            frameFullScreen.removeAllViews();
        }
        subscriber = null;
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
    }

}
