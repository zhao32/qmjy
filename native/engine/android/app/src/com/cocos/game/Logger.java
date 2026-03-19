package com.cocos.game;

import android.util.Log;

/**
 * 1.v(verbose)：任何信息都会输出
 * 2.d(debug)：输出调试信息
 * 3.w(warning)：输出警告信息
 * 4.i(info)：输出提示信息
 * 5.e(error)：输出错误信息
 */
public class Logger {
    private static final boolean DEBUG = true;

    private static final String LOGGER = Logger.class.getSimpleName();
    private static final int MAX_LENGTH = 2000; // MAX：4*1024

    public static void v(String tag, String msg) {
        if (DEBUG) {
            int length = msg.length();
            if (length > MAX_LENGTH) {
                for (int i = 0; i < length; i += MAX_LENGTH) {
                    if (i + MAX_LENGTH < length) {
                        Log.v(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, i + MAX_LENGTH));
                    } else {
                        Log.v(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, length));
                    }
                }
            } else {
                Log.v(String.format("%1$s_%2$s", LOGGER, tag), msg);
            }
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            int length = msg.length();
            if (length > MAX_LENGTH) {
                for (int i = 0; i < length; i += MAX_LENGTH) {
                    if (i + MAX_LENGTH < length) {
                        Log.v(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, i + MAX_LENGTH), tr);
                    } else {
                        Log.v(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, length), tr);
                    }
                }
            } else {
                Log.v(String.format("%1$s_%2$s", LOGGER, tag), msg, tr);
            }
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            int length = msg.length();
            if (length > MAX_LENGTH) {
                for (int i = 0; i < length; i += MAX_LENGTH) {
                    if (i + MAX_LENGTH < length) {
                        Log.d(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, i + MAX_LENGTH));
                    } else {
                        Log.d(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, length));
                    }
                }
            } else {
                Log.d(String.format("%1$s_%2$s", LOGGER, tag), msg);
            }
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            int length = msg.length();
            if (length > MAX_LENGTH) {
                for (int i = 0; i < length; i += MAX_LENGTH) {
                    if (i + MAX_LENGTH < length) {
                        Log.d(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, i + MAX_LENGTH), tr);
                    } else {
                        Log.d(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, length), tr);
                    }
                }
            } else {
                Log.d(String.format("%1$s_%2$s", LOGGER, tag), msg, tr);
            }
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            int length = msg.length();
            if (length > MAX_LENGTH) {
                for (int i = 0; i < length; i += MAX_LENGTH) {
                    if (i + MAX_LENGTH < length) {
                        Log.i(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, i + MAX_LENGTH));
                    } else {
                        Log.i(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, length));
                    }
                }
            } else {
                Log.i(String.format("%1$s_%2$s", LOGGER, tag), msg);
            }
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            int length = msg.length();
            if (length > MAX_LENGTH) {
                for (int i = 0; i < length; i += MAX_LENGTH) {
                    if (i + MAX_LENGTH < length) {
                        Log.i(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, i + MAX_LENGTH), tr);
                    } else {
                        Log.i(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, length), tr);
                    }
                }
            } else {
                Log.i(String.format("%1$s_%2$s", LOGGER, tag), msg, tr);
            }
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            int length = msg.length();
            if (length > MAX_LENGTH) {
                for (int i = 0; i < length; i += MAX_LENGTH) {
                    if (i + MAX_LENGTH < length) {
                        Log.w(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, i + MAX_LENGTH));
                    } else {
                        Log.w(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, length));
                    }
                }
            } else {
                Log.w(String.format("%1$s_%2$s", LOGGER, tag), msg);
            }
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            int length = msg.length();
            if (length > MAX_LENGTH) {
                for (int i = 0; i < length; i += MAX_LENGTH) {
                    if (i + MAX_LENGTH < length) {
                        Log.w(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, i + MAX_LENGTH), tr);
                    } else {
                        Log.w(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, length), tr);
                    }
                }
            } else {
                Log.w(String.format("%1$s_%2$s", LOGGER, tag), msg, tr);
            }
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            int length = msg.length();
            if (length > MAX_LENGTH) {
                for (int i = 0; i < length; i += MAX_LENGTH) {
                    if (i + MAX_LENGTH < length) {
                        Log.e(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, i + MAX_LENGTH));
                    } else {
                        Log.e(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, length));
                    }
                }
            } else {
                Log.e(String.format("%1$s_%2$s", LOGGER, tag), msg);
            }
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            int length = msg.length();
            if (length > MAX_LENGTH) {
                for (int i = 0; i < length; i += MAX_LENGTH) {
                    if (i + MAX_LENGTH < length) {
                        Log.e(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, i + MAX_LENGTH), tr);
                    } else {
                        Log.e(String.format("%1$s_%2$s", LOGGER, tag), msg.substring(i, length), tr);
                    }
                }
            } else {
                Log.e(String.format("%1$s_%2$s", LOGGER, tag), msg, tr);
            }
        }
    }
}
