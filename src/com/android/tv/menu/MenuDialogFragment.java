/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tv.menu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.tv.TvInputInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.android.tv.R;
import com.android.tv.TvActivity;
import com.android.tv.Utils;

/**
 * The TV app's menu dialog.
 */
public final class MenuDialogFragment extends DialogFragment {
    public static final String DIALOG_TAG = MenuDialogFragment.class.getName();
    public static final boolean PIP_MENU_ENABLED = true;

    public static final String ARG_CURRENT_INPUT = "current_input";
    public static final String ARG_IS_UNIFIED_TV_INPUT = "unified_tv_input";

    private static final int POSITION_SELECT_INPUT  = 0;
    private static final int POSITION_EDIT_CHANNELS = 1;
    private static final int POSITION_SETUP         = 2;
    private static final int POSITION_PRIVACY       = 3;
    private static final int POSITION_PIP           = 4;
    private static final int POSITION_SETTINGS      = 5;

    private TvInputInfo mCurrentInput;
    private boolean mIsUnifiedTvInput;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arg = getArguments();
        if (arg != null) {
            mCurrentInput = arg.getParcelable(ARG_CURRENT_INPUT);
            mIsUnifiedTvInput = arg.getBoolean(ARG_IS_UNIFIED_TV_INPUT);
        } else {
            mIsUnifiedTvInput = false;
        }

        String[] items = {
                getString(R.string.menu_select_input),
                getString(R.string.menu_edit_channels),
                getString(R.string.menu_auto_scan),
                getString(R.string.menu_privacy_setting),
                getString(R.string.menu_toggle_pip),
                getString(R.string.source_specific_setting)
        };

        ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, items) {
                    @Override
                    public boolean areAllItemsEnabled() {
                        return false;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        view.setEnabled(isEnabled(position));
                        return view;
                    }

                    @Override
                    public boolean isEnabled(int position) {
                        switch (position) {
                            case POSITION_EDIT_CHANNELS:
                                return mCurrentInput != null;
                            case POSITION_SETUP:
                                return !mIsUnifiedTvInput && mCurrentInput != null
                                        && Utils.hasActivity(getContext(),
                                                mCurrentInput, Utils.ACTION_SETUP);
                            case POSITION_PIP:
                                return PIP_MENU_ENABLED;
                            case POSITION_SETTINGS:
                                return !mIsUnifiedTvInput && mCurrentInput != null
                                        && Utils.hasActivity(getContext(),
                                                mCurrentInput, Utils.ACTION_SETTINGS);
                        }
                        return true;
                    }
        };

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.menu_title)
                .setAdapter(adapter, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case POSITION_SELECT_INPUT:
                                dismiss();
                                ((TvActivity) getActivity()).showInputPickerDialog();
                                break;
                            case POSITION_EDIT_CHANNELS:
                                dismiss();
                                ((TvActivity) getActivity()).showEditChannelsDialog();
                                break;
                            case POSITION_SETUP:
                                dismiss();
                                ((TvActivity) getActivity()).startSetupActivity(mCurrentInput);
                                break;
                            case POSITION_PRIVACY:
                                showDialogFragment(PrivacySettingDialogFragment.DIALOG_TAG,
                                        new PrivacySettingDialogFragment());
                                break;
                            case POSITION_PIP:
                                TvActivity activity = (TvActivity) getActivity();
                                activity.togglePipView();
                                break;
                            case POSITION_SETTINGS:
                                dismiss();
                                ((TvActivity) getActivity()).startSettingsActivity();
                                break;
                        }
                    }
                })
                .create();
    }

    private void showDialogFragment(String tag, DialogFragment dialog) {
        dismiss();

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialog.show(ft, tag);
    }
}
