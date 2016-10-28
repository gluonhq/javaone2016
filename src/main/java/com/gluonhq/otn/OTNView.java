/**
 * Copyright (c) 2016, Gluon Software
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse
 *    or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.otn;

import com.gluonhq.charm.glisten.afterburner.AppView;
import com.gluonhq.charm.glisten.afterburner.AppViewRegistry;
import com.gluonhq.charm.glisten.afterburner.GluonPresenter;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.gluonhq.otn.util.OTNBundle;
import com.gluonhq.otn.views.*;

import static com.gluonhq.charm.glisten.afterburner.AppView.Flag.*;

public class OTNView  {

    public static final AppViewRegistry registry = new AppViewRegistry();

    public static final AppView ACTIVITY_FEED  = view( ActivityFeedPresenter.class,  MaterialDesignIcon.ANNOUNCEMENT,       SHOW_IN_DRAWER, HOME_VIEW, SKIP_VIEW_STACK);
    public static final AppView SESSIONS       = view( SessionsPresenter.class,      MaterialDesignIcon.DASHBOARD,          SHOW_IN_DRAWER);
    public static final AppView SESSION        = view( SessionPresenter.class,       MaterialDesignIcon.RECORD_VOICE_OVER);
    public static final AppView SPEAKERS       = view( SpeakersPresenter.class,      MaterialDesignIcon.SPEAKER,            SHOW_IN_DRAWER);
    public static final AppView SPEAKER        = view( SpeakerPresenter.class,       MaterialDesignIcon.SPEAKER);
    public static final AppView EXHIBITORS     = view( ExhibitorsPresenter.class,    MaterialDesignIcon.PLACE,              SHOW_IN_DRAWER);
    public static final AppView EXHIBITOR      = view( ExhibitorPresenter.class,     MaterialDesignIcon.PLACE);
    public static final AppView EXHIBITION_MAP = view( ExhibitionMapPresenter.class, MaterialDesignIcon.MAP,                SHOW_IN_DRAWER);
    public static final AppView SPONSORS       = view( SponsorsPresenter.class,      MaterialDesignIcon.PAYMENT/*,            SHOW_IN_DRAWER*/);
    public static final AppView SPONSOR        = view( SponsorPresenter.class,       MaterialDesignIcon.PAYMENT);
    public static final AppView VENUES         = view( VenuesPresenter.class,        MaterialDesignIcon.ACCESSIBILITY,      SHOW_IN_DRAWER);
    public static final AppView VENUE          = view( VenuePresenter.class,         MaterialDesignIcon.ACCESSIBILITY);
    public static final AppView EXPERIENCES    = view( ExperiencesPresenter.class,   MaterialDesignIcon.VIEW_AGENDA,        SHOW_IN_DRAWER);
    public static final AppView BADGE          = view( BadgePresenter.class,         MaterialDesignIcon.ALL_INCLUSIVE);
    public static final AppView COFFEE         = view( CoffeePresenter.class,        MaterialDesignIcon.LOCAL_DRINK);
    public static final AppView GAME           = view( GamePresenter.class,          MaterialDesignIcon.GAMES);
    public static final AppView VOTE3D         = view( Vote3DPresenter.class,        MaterialDesignIcon._3D_ROTATION);
    public static final AppView EMBROIDER      = view( EmbroiderPresenter.class,     MaterialDesignIcon.GESTURE);
    public static final AppView IOT_WORKSHOP   = view( IOTWorkshopPresenter.class,   MaterialDesignIcon.DEVELOPER_BOARD);
    public static final AppView NOTES          = view( NotesPresenter.class,         MaterialDesignIcon.EVENT_NOTE,         SHOW_IN_DRAWER);
    public static final AppView NOTIFICATIONS  = view( NotificationsPresenter.class, MaterialDesignIcon.NOTIFICATIONS);
    public static final AppView UNIVERSITY     = view( UniversityPresenter.class,    MaterialDesignIcon.SCHOOL,             SHOW_IN_DRAWER);
    public static final AppView SEARCH         = view( SearchPresenter.class,        MaterialDesignIcon.SEARCH);
    public static final AppView EULA           = view( EulaPresenter.class,          MaterialDesignIcon.ASSESSMENT);
    public static final AppView ABOUT          = view( AboutPresenter.class,         MaterialDesignIcon.AC_UNIT,            SHOW_IN_DRAWER);

    static AppView view(Class<? extends GluonPresenter<?>> presenterClass, MaterialDesignIcon menuIcon, AppView.Flag... flags ) {
        return registry.createView( name(presenterClass),
                                    OTNBundle.getString( "OTN.VIEW." + name(presenterClass)),
                                    presenterClass,
                                    menuIcon,
                                    flags);
    }

    private static String name(Class<? extends GluonPresenter<?>> presenterClass) {
        return presenterClass.getSimpleName().toUpperCase().replace("PRESENTER", "");
    }

}
