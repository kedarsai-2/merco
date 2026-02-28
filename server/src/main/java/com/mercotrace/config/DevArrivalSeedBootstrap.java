package com.mercotrace.config;

import com.mercotrace.config.Constants;
import com.mercotrace.domain.*;
import com.mercotrace.repository.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Dev-only bootstrap: seeds one vehicle with weight, two contacts, two commodities,
 * two seller-in-vehicle links, and 4–6 lots for trader 101 so that
 * GET /api/module-auctions/lots returns data after startup.
 */
@Component
@Profile("dev")
public class DevArrivalSeedBootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(DevArrivalSeedBootstrap.class);
    private static final long SEED_TRADER_ID = 101L;

    private final LotRepository lotRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleWeightRepository vehicleWeightRepository;
    private final ContactRepository contactRepository;
    private final CommodityRepository commodityRepository;
    private final SellerInVehicleRepository sellerInVehicleRepository;

    public DevArrivalSeedBootstrap(
        LotRepository lotRepository,
        VehicleRepository vehicleRepository,
        VehicleWeightRepository vehicleWeightRepository,
        ContactRepository contactRepository,
        CommodityRepository commodityRepository,
        SellerInVehicleRepository sellerInVehicleRepository
    ) {
        this.lotRepository = lotRepository;
        this.vehicleRepository = vehicleRepository;
        this.vehicleWeightRepository = vehicleWeightRepository;
        this.contactRepository = contactRepository;
        this.commodityRepository = commodityRepository;
        this.sellerInVehicleRepository = sellerInVehicleRepository;
    }

    @PostConstruct
    public void seedIfEmpty() {
        if (!lotRepository.findAllByTraderId(SEED_TRADER_ID, PageRequest.of(0, 1)).isEmpty()) {
            LOG.debug("Dev arrival seed skipped: trader {} already has lots", SEED_TRADER_ID);
            return;
        }

        Instant now = Instant.now();
        String system = Constants.SYSTEM;

        // 1. Vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setTraderId(SEED_TRADER_ID);
        vehicle.setVehicleNumber("SEED-VEH-001");
        vehicle.setArrivalDatetime(now);
        vehicle.setCreatedBy(system);
        vehicle.setCreatedAt(now);
        vehicle = vehicleRepository.save(vehicle);

        // 2. VehicleWeight
        VehicleWeight weight = new VehicleWeight();
        weight.setVehicleId(vehicle.getId());
        weight.setLoadedWeight(1000.0);
        weight.setEmptyWeight(100.0);
        weight.setDeductedWeight(0.0);
        weight.setNetWeight(900.0);
        weight.setRecordedAt(now);
        vehicleWeightRepository.save(weight);

        // 3. Contacts (trader 101)
        Contact contact1 = new Contact();
        contact1.setTraderId(SEED_TRADER_ID);
        contact1.setName("Seed Seller One");
        contact1.setPhone("9000000001");
        contact1.setMark("S1");
        contact1.setOpeningBalance(BigDecimal.ZERO);
        contact1.setCurrentBalance(BigDecimal.ZERO);
        contact1.setCreatedAt(now);
        contact1 = contactRepository.save(contact1);

        Contact contact2 = new Contact();
        contact2.setTraderId(SEED_TRADER_ID);
        contact2.setName("Seed Seller Two");
        contact2.setPhone("9000000002");
        contact2.setMark("S2");
        contact2.setOpeningBalance(BigDecimal.ZERO);
        contact2.setCurrentBalance(BigDecimal.ZERO);
        contact2.setCreatedAt(now);
        contact2 = contactRepository.save(contact2);

        // 4. Commodities (trader 101)
        Commodity onion = new Commodity();
        onion.setTraderId(SEED_TRADER_ID);
        onion.setCommodityName("Onion");
        onion.setCreatedBy(system);
        onion.setCreatedAt(now);
        onion = commodityRepository.save(onion);

        Commodity tomato = new Commodity();
        tomato.setTraderId(SEED_TRADER_ID);
        tomato.setCommodityName("Tomato");
        tomato.setCreatedBy(system);
        tomato.setCreatedAt(now);
        tomato = commodityRepository.save(tomato);

        // 5. SellerInVehicle (one per contact, same vehicle)
        SellerInVehicle siv1 = new SellerInVehicle();
        siv1.setVehicleId(vehicle.getId());
        siv1.setContactId(contact1.getId());
        siv1.setCreatedBy(system);
        siv1 = sellerInVehicleRepository.save(siv1);

        SellerInVehicle siv2 = new SellerInVehicle();
        siv2.setVehicleId(vehicle.getId());
        siv2.setContactId(contact2.getId());
        siv2.setCreatedBy(system);
        siv2 = sellerInVehicleRepository.save(siv2);

        // 6. Lots (4–6): alternate seller_vehicle_id, commodity, lot_name Lot-1..Lot-6, bag_count 30/20/25, seller_serial_no 1..6
        List<SellerInVehicle> sellers = List.of(siv1, siv2);
        List<Commodity> commodities = List.of(onion, tomato);
        int[] bagCounts = { 30, 20, 25, 30, 20, 25 };
        for (int i = 0; i < 6; i++) {
            Lot lot = new Lot();
            lot.setSellerVehicleId(sellers.get(i % 2).getId());
            lot.setCommodityId(commodities.get(i % 2).getId());
            lot.setLotName("Lot-" + (i + 1));
            lot.setBagCount(bagCounts[i]);
            lot.setSellerSerialNo(i + 1);
            lot.setCreatedBy(system);
            lot.setCreatedAt(now);
            lotRepository.save(lot);
        }

        LOG.info("Dev arrival seed created for trader {}: 1 vehicle, 2 contacts, 2 commodities, 2 sellers, 6 lots", SEED_TRADER_ID);
    }
}
