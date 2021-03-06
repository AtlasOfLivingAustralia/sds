package au.org.ala.sds.util;

import au.org.ala.names.model.RankType;
import au.org.ala.sds.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class GeneralisedLocationTest {

    String NSW_LAT = "-35.12345",  NSW_LONG = "146.67890";
    String VIC_LAT = "-37.046409", VIC_LONG = "146.239014";
    String QLD_LAT = "-18.406655", QLD_LONG = "146.2541";
    String TAS_LAT = "-42.067518", TAS_LONG = "145.278568";
    String ACT_LAT = "-35.29187",  ACT_LONG = "149.100137";
    static SensitiveTaxon st;
    static List<SensitivityInstance> instances;
    static SensitivityZone ACT;
    static SensitivityZone NSW;
    static SensitivityZone VIC;
    static SensitivityZone QLD;
    static SensitivityZone TAS;
    static List<SensitivityZone> zones;
    static SensitivityCategory CRITICALLY_ENDANGERED;
    static SensitivityCategory ENDANGERED;
    static SensitivityCategory VULNERABLE;
    static SensitivityCategory NEAR_THREATENED;

    static {
        st = new SensitiveTaxon("Crex crex", RankType.SPECIES);
        instances = st.getInstances();
        ACT = SensitivityZoneFactory.getZone(SensitivityZone.ACT);
        NSW = SensitivityZoneFactory.getZone(SensitivityZone.NSW);
        VIC = SensitivityZoneFactory.getZone(SensitivityZone.VIC);
        QLD = SensitivityZoneFactory.getZone(SensitivityZone.QLD);
        TAS = SensitivityZoneFactory.getZone(SensitivityZone.TAS);
        CRITICALLY_ENDANGERED = SensitivityCategoryFactory.getCategory(SensitivityCategory.CRITICALLY_ENDANGERED);
        ENDANGERED = SensitivityCategoryFactory.getCategory(SensitivityCategory.ENDANGERED);
        VULNERABLE = SensitivityCategoryFactory.getCategory(SensitivityCategory.VULNERABLE);
        NEAR_THREATENED = SensitivityCategoryFactory.getCategory(SensitivityCategory.NEAR_THREATENED);
        instances.add(new ConservationInstance(CRITICALLY_ENDANGERED, "NSW OEH", "dr487", NSW, null, null, "WITHHOLD"));
        instances.add(new ConservationInstance(ENDANGERED, "Vic DSE", "dr490", VIC, null, null, "10km"));
        instances.add(new ConservationInstance(VULNERABLE, "QLD DEHP", "dr493", QLD, null, null, "1km"));
        instances.add(new ConservationInstance(NEAR_THREATENED, "Tas DPIPWE", "dr491", TAS, null, null, "100m"));
    }

    @Test
    public void locationGeneralisation() {
        zones = new ArrayList<SensitivityZone>(Collections.singleton(NSW));
        GeneralisedLocation extremeGeneralisation = GeneralisedLocationFactory.getGeneralisedLocation(NSW_LAT, NSW_LONG, instances, zones);
        assertEquals("", extremeGeneralisation.getGeneralisedLatitude());
        assertEquals("", extremeGeneralisation.getGeneralisedLongitude());
        assertEquals("", extremeGeneralisation.getGeneralisationInMetres());

        zones = new ArrayList<SensitivityZone>(Collections.singleton(VIC));
        GeneralisedLocation highGeneralisation = GeneralisedLocationFactory.getGeneralisedLocation(VIC_LAT, VIC_LONG, instances, zones);
        assertEquals("-37.0", highGeneralisation.getGeneralisedLatitude());
        assertEquals("146.2", highGeneralisation.getGeneralisedLongitude());
        assertEquals("10000", highGeneralisation.getGeneralisationInMetres());

        zones = new ArrayList<SensitivityZone>(Collections.singleton(QLD));
        GeneralisedLocation mediumGeneralisation = GeneralisedLocationFactory.getGeneralisedLocation(QLD_LAT, QLD_LONG, instances, zones);
        assertEquals("-18.41", mediumGeneralisation.getGeneralisedLatitude());
        assertEquals("146.25", mediumGeneralisation.getGeneralisedLongitude());
        assertEquals("1000", mediumGeneralisation.getGeneralisationInMetres());

        zones = new ArrayList<SensitivityZone>(Collections.singleton(TAS));
        GeneralisedLocation lowGeneralisation = GeneralisedLocationFactory.getGeneralisedLocation(TAS_LAT, TAS_LONG, instances, zones);
        assertEquals("-42.068", lowGeneralisation.getGeneralisedLatitude());
        assertEquals("145.279", lowGeneralisation.getGeneralisedLongitude());
        assertEquals("100", lowGeneralisation.getGeneralisationInMetres());

        zones = new ArrayList<SensitivityZone>(Collections.singleton(ACT));
        GeneralisedLocation defaultGeneralisation = GeneralisedLocationFactory.getGeneralisedLocation(ACT_LAT, ACT_LONG, instances, zones);
        assertEquals(ACT_LAT, defaultGeneralisation.getGeneralisedLatitude());
        assertEquals(ACT_LONG, defaultGeneralisation.getGeneralisedLongitude());
        assertEquals("", defaultGeneralisation.getGeneralisationInMetres());
    }
}
