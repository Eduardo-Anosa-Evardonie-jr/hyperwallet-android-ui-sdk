/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Hyperwallet Systems Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.hyperwallet.android.ui.transfermethod.view.widget;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.hyperwallet.android.model.graphql.field.Field;

/**
 * Provides additional methods for Widget to help with formatting for input types.
 */
abstract class AbstractMaskedInputWidget extends AbstractWidget {
    private static final char NUMBER_TOKEN = '#';
    private static final char ENGLISH_LETTER_TOKEN = '@';
    private static final char LETTER_OR_NUMBER_TOKEN = '*';
    private static final char BACKSLASH_ESCAPED = '\\';

    ViewGroup mContainer;
    String mValue;
    TextInputLayout mTextInputLayout;

    AbstractMaskedInputWidget(@Nullable Field field, @NonNull WidgetEventListener listener,
            @Nullable String defaultValue, @NonNull View defaultFocusView) {
        super(field, listener, defaultValue, defaultFocusView);
    }

    /**
     * Removes the characters matching in the field's scrubRegex. This is to prepare the data for submission to the
     * server.
     *
     * @param displayValue the value that would be coming from the UI component
     * @return the raw value that will be sent to the server for submission and validation
     */
    String formatToApi(@NonNull final String displayValue) {
        if (mField.getMask() != null && mField.getMask().getScrubRegex() != null) {
            return displayValue.replaceAll(mField.getMask().getScrubRegex(), "");
        }
        return displayValue;
    }

    /**
     * Uses the input and returns the formatted value specified in the mask pattern. Uses the conditional pattern as
     * necessary.
     *
     * @param apiValue data in raw form
     * @return a String formatted to the specification in the mask pattern
     */
    String formatToDisplay(@NonNull final String apiValue) {
        if (mField != null && mField.getMask() != null && !mField.isFieldValueMasked()) {
            // format
            String pattern = mField.getMask().getPattern(apiValue);
            if (!TextUtils.isEmpty(pattern)) {
                return format(apiValue, pattern);
            } else {
                return apiValue;
            }
        }
        return apiValue;
    }

    String format(@NonNull final String apiValue, @NonNull final String pattern) {
        StringBuilder formattedValue = new StringBuilder();
        String extraTokens = "";
        String backslash = "";
        int patternIndex = 0;
        int textIndex = 0;

        // Please ensure one of patternIndex or textIndex is incremented for each iteration to prevent an infinite loop
        while (true) {
            if (textIndex >= apiValue.length() || patternIndex >= pattern.length()) {
                break;
            }
            char token = pattern.charAt(patternIndex);
            char textChar = apiValue.charAt(textIndex);

            if (backslash.length() == 1) {
                if (token == textChar) {
                    if (extraTokens.length() > 0) {
                        formattedValue.append(extraTokens);
                        extraTokens = "";
                    }
                    formattedValue.append(textChar);
                    textIndex++;
                } else {
                    extraTokens += token;
                }
                patternIndex++;
                backslash = "";

            } else if (token == BACKSLASH_ESCAPED) {
                backslash += token;
                patternIndex++;

            } else if (extraTokens.length() == 1 && textChar == extraTokens.charAt(0)) {
                formattedValue.append(textChar);
                extraTokens = "";
                textIndex++;

            } else {
                switch (token) {
                    case NUMBER_TOKEN:
                        if (Character.isDigit(textChar)) {
                            if (extraTokens.length() > 0) {
                                formattedValue.append(extraTokens);
                                extraTokens = "";
                            }
                            formattedValue.append(textChar);
                            patternIndex++;
                        }
                        textIndex++;
                        break;
                    case ENGLISH_LETTER_TOKEN:
                        if ((textChar >= 'a' && textChar <= 'z') || (textChar >= 'A' && textChar <= 'Z')) {
                            if (extraTokens.length() > 0) {
                                formattedValue.append(extraTokens);
                                extraTokens = "";
                            }
                            formattedValue.append(textChar);
                            patternIndex++;
                        }
                        textIndex++;
                        break;
                    case LETTER_OR_NUMBER_TOKEN:
                        if (Character.isDigit(textChar)
                                || (textChar >= 'a' && textChar <= 'z')
                                || (textChar >= 'A' && textChar <= 'Z')) {
                            if (extraTokens.length() > 0) {
                                formattedValue.append(extraTokens);
                                extraTokens = "";
                            }
                            formattedValue.append(textChar);
                            patternIndex++;
                        }
                        textIndex++;
                        break;
                    default:
                        if (token == textChar) {
                            if (extraTokens.length() > 0) {
                                formattedValue.append(extraTokens);
                                extraTokens = "";
                            }
                            formattedValue.append(textChar);
                            textIndex++;
                        } else {
                            extraTokens += token;
                        }
                        patternIndex++;
                }
            }
        }
        return formattedValue.toString();
    }

    class InputMaskTextWatcher implements TextWatcher {

        final EditText mEditText;

        InputMaskTextWatcher(@NonNull final EditText editText) {
            mEditText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (before != count) {
                String displayedValue = formatToDisplay(s.toString());
                int cursorIndex = start + count + (displayedValue.length() - s.length());
                mEditText.setText(displayedValue);
                if (cursorIndex >= 0 && cursorIndex <= displayedValue.length()) {
                    mEditText.setSelection(cursorIndex);
                } else if (cursorIndex < 0) {
                    mEditText.setSelection(0);
                }

                mValue = formatToApi(displayedValue);
                mListener.saveTextChanged(getName(), getValue());
            }
        }
    }

    /**
     * Text widget needs more control on updating text on edit text within watcher
     * to avoid re-triggering the watcher an additional routine is executed during
     * {@link #afterTextChanged(Editable)} is being invoked
     */
    class TextWidgetWatcher extends InputMaskTextWatcher {

        TextWidgetWatcher(@NonNull final EditText editText) {
            super(editText);
        }

        /*
         * @see {@link TextWatcher#afterTextChanged(Editable)}
         */
        @Override
        public void afterTextChanged(Editable s) {
            if (mValue != null) {
                String displayValue = formatToDisplay(mValue);
                if (displayValue != null) {
                    mEditText.removeTextChangedListener(this);
                    s.replace(0, s.length(), displayValue);
                    mValue = formatToApi(displayValue);
                    mEditText.addTextChangedListener(this);
                }
            }
        }

        /*
         * @see {@link TextWatcher#onTextChanged(CharSequence, int, int, int)}
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (before != count) {
                mValue = formatToApi(s.toString());
                mListener.saveTextChanged(getName(), getValue());
            }
        }
    }
}