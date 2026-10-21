package it.uniroma3.siw.travelly.service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.travelly.dto.ExpenseDashboardResponse;
import it.uniroma3.siw.travelly.dto.ExpenseDashboardResponse.CategoryTotal;
import it.uniroma3.siw.travelly.dto.ExpenseDashboardResponse.ParticipantBalance;
import it.uniroma3.siw.travelly.dto.ExpenseDashboardResponse.Settlement;
import it.uniroma3.siw.travelly.model.Expense;
import it.uniroma3.siw.travelly.model.ExpenseCategory;
import it.uniroma3.siw.travelly.model.Participant;
import it.uniroma3.siw.travelly.model.Trip;
import it.uniroma3.siw.travelly.repository.TripRepository;


@Service
public class ExpenseDashboardService {

    private final TripRepository tripRepository;


    public ExpenseDashboardService(
            TripRepository tripRepository) {

        this.tripRepository = tripRepository;
    }


    /*
     * Costruisce tutti i dati necessari
     * per la dashboard economica del viaggio.
     *
     * Il Trip viene recuperato solamente se
     * appartiene all'utente autenticato.
     */
    @Transactional(readOnly = true)
    public Optional<ExpenseDashboardResponse> getDashboard(
            Long tripId,
            String ownerUsername) {


        return tripRepository
                .findByIdAndOwnerUsername(
                        tripId,
                        ownerUsername
                )
                .map(this::buildDashboard);
    }


    /*
     * Costruisce effettivamente il riepilogo.
     */
    private ExpenseDashboardResponse buildDashboard(
            Trip trip) {


        /*
         * Copiamo i partecipanti in una lista.
         *
         * Manteniamo l'ordine del viaggio.
         */
        List<Participant> participants =
                new ArrayList<>(
                        trip.getParticipants()
                );


        /*
         * Mappa:
         *
         * participantId -> totale pagato
         *
         * Usiamo i centesimi per evitare problemi
         * di arrotondamento durante i calcoli.
         */
        Map<Long, Long> paidByParticipant =
                new LinkedHashMap<>();


        for (Participant participant : participants) {

            paidByParticipant.put(
                    participant.getId(),
                    0L
            );
        }


        /*
         * Totale per categoria.
         */
        Map<ExpenseCategory, Long> categoryTotals =
                new EnumMap<>(
                        ExpenseCategory.class
                );


        long totalCents = 0;

        long unassignedCents = 0;


        /*
         * Analizziamo tutte le Expense del viaggio.
         *
         * ATTENZIONE:
         *
         * usiamo solamente Expense.
         *
         * NON sommiamo Activity.cost perché le Activity
         * a pagamento generano già una Expense.
         */
        for (Expense expense : trip.getExpenses()) {


            long amountCents =
                    toCents(
                            expense.getAmount()
                    );


            /*
             * Ignoriamo eventuali valori nulli
             * o non positivi anomali.
             */
            if (amountCents <= 0) {
                continue;
            }


            /*
             * Totale generale.
             */
            totalCents += amountCents;


            /*
             * Totale per categoria.
             */
            if (expense.getCategory() != null) {

                categoryTotals.merge(
                        expense.getCategory(),
                        amountCents,
                        Long::sum
                );
            }


            /*
             * Totale pagato per partecipante.
             */
            if (expense.getPaidBy() == null) {

                unassignedCents += amountCents;
            }

            else {

                Long participantId =
                        expense.getPaidBy()
                                .getId();


                /*
                 * Il partecipante deve essere
                 * effettivamente presente nel viaggio.
                 *
                 * Se per qualche vecchio dato incoerente
                 * non lo fosse, trattiamo la spesa
                 * come non assegnata.
                 */
                if (paidByParticipant
                        .containsKey(participantId)) {


                    paidByParticipant.merge(
                            participantId,
                            amountCents,
                            Long::sum
                    );
                }

                else {

                    unassignedCents +=
                            amountCents;
                }
            }
        }


        /*
         * Prepariamo i dati del grafico a torta:
         *
         * spese per categoria.
         */
        List<CategoryTotal> categories =
                new ArrayList<>();


        for (Map.Entry<ExpenseCategory, Long> entry :
                categoryTotals.entrySet()) {


            categories.add(
                    new CategoryTotal(
                            entry.getKey().name(),
                            toEuros(entry.getValue())
                    )
            );
        }


        /*
         * Se non esistono partecipanti,
         * non possiamo dividere le spese.
         */
        if (participants.isEmpty()) {


            return new ExpenseDashboardResponse(

                    toEuros(totalCents),

                    toEuros(unassignedCents),

                    false,

                    "Aggiungi almeno un partecipante per calcolare la divisione delle spese.",

                    categories,

                    buildParticipantListWithoutSettlement(
                            participants,
                            paidByParticipant
                    ),

                    List.of()
            );
        }


        /*
         * Se esistono spese senza pagatore
         * non possiamo ancora calcolare correttamente
         * chi deve soldi a chi.
         */
        if (unassignedCents > 0) {


            return new ExpenseDashboardResponse(

                    toEuros(totalCents),

                    toEuros(unassignedCents),

                    false,

                    "Assegna un pagatore a tutte le spese per calcolare chi deve soldi a chi.",

                    categories,

                    buildParticipantListWithoutSettlement(
                            participants,
                            paidByParticipant
                    ),

                    List.of()
            );
        }


        /*
         * Tutte le spese sono assegnate.
         *
         * Possiamo quindi calcolare:
         *
         * - quota equa
         * - saldo di ogni partecipante
         * - trasferimenti necessari
         */


        int participantCount =
                participants.size();


        /*
         * Divisione in centesimi.
         *
         * Esempio:
         *
         * 1000 cent / 3 persone
         *
         * baseShare = 333
         * remainder = 1
         *
         * quindi:
         *
         * primo partecipante 334
         * altri              333
         *
         * In questo modo non perdiamo nemmeno 1 centesimo.
         */
        long baseShare =
                totalCents / participantCount;


        long remainder =
                totalCents % participantCount;


        List<ParticipantBalance> participantBalances =
                new ArrayList<>();


        /*
         * Liste interne usate per il calcolo
         * dei trasferimenti.
         */
        List<BalanceHolder> creditors =
                new ArrayList<>();


        List<BalanceHolder> debtors =
                new ArrayList<>();


        for (int i = 0;
             i < participants.size();
             i++) {


            Participant participant =
                    participants.get(i);


            long paidCents =
                    paidByParticipant.getOrDefault(
                            participant.getId(),
                            0L
                    );


            /*
             * Distribuiamo eventuali centesimi
             * di resto ai primi partecipanti.
             */
            long fairShareCents =
                    baseShare;


            if (i < remainder) {

                fairShareCents++;
            }


            /*
             * saldo = pagato - quota dovuta
             */
            long balanceCents =
                    paidCents -
                    fairShareCents;


            String status;


            if (balanceCents > 0) {

                status = "CREDITORE";


                creditors.add(
                        new BalanceHolder(
                                participant.getId(),
                                participant.getName(),
                                balanceCents
                        )
                );
            }

            else if (balanceCents < 0) {

                status = "DEBITORE";


                debtors.add(
                        new BalanceHolder(
                                participant.getId(),
                                participant.getName(),
                                balanceCents
                        )
                );
            }

            else {

                status = "IN_PARI";
            }


            participantBalances.add(
                    new ParticipantBalance(

                            participant.getId(),

                            participant.getName(),

                            toEuros(
                                    paidCents
                            ),

                            toEuros(
                                    fairShareCents
                            ),

                            toEuros(
                                    balanceCents
                            ),

                            status
                    )
            );
        }


        /*
         * Calcoliamo chi deve pagare chi.
         */
        List<Settlement> settlements =
                calculateSettlements(
                        creditors,
                        debtors
                );


        return new ExpenseDashboardResponse(

                toEuros(totalCents),

                0.0,

                true,

                "Le spese sono divise in parti uguali tra tutti i partecipanti.",

                categories,

                participantBalances,

                settlements
        );
    }


    /*
     * Costruisce la lista partecipanti quando
     * il calcolo dei debiti non è ancora disponibile.
     */
    private List<ParticipantBalance>
            buildParticipantListWithoutSettlement(

                    List<Participant> participants,

                    Map<Long, Long> paidByParticipant) {


        List<ParticipantBalance> result =
                new ArrayList<>();


        for (Participant participant : participants) {


            long paidCents =
                    paidByParticipant.getOrDefault(
                            participant.getId(),
                            0L
                    );


            result.add(
                    new ParticipantBalance(

                            participant.getId(),

                            participant.getName(),

                            toEuros(
                                    paidCents
                            ),

                            null,

                            null,

                            null
                    )
            );
        }


        return result;
    }


    /*
     * Calcola i trasferimenti necessari
     * per pareggiare i conti.
     *
     * Esempio:
     *
     * Alessia +200
     * Gabri   -200
     *
     * risultato:
     *
     * Gabri -> Alessia : 200 €
     */
    private List<Settlement> calculateSettlements(

            List<BalanceHolder> creditors,

            List<BalanceHolder> debtors) {


        List<Settlement> settlements =
                new ArrayList<>();


        int creditorIndex = 0;

        int debtorIndex = 0;


        while (
                creditorIndex < creditors.size()
                &&
                debtorIndex < debtors.size()
        ) {


            BalanceHolder creditor =
                    creditors.get(
                            creditorIndex
                    );


            BalanceHolder debtor =
                    debtors.get(
                            debtorIndex
                    );


            /*
             * Il debitore ha balance negativo.
             *
             * Convertiamo il suo debito
             * in valore positivo.
             */
            long debtorNeedsToPay =
                    -debtor.balanceCents;


            long creditorNeedsToReceive =
                    creditor.balanceCents;


            /*
             * Trasferiamo il minimo dei due.
             */
            long transferCents =
                    Math.min(
                            debtorNeedsToPay,
                            creditorNeedsToReceive
                    );


            settlements.add(
                    new Settlement(

                            debtor.participantId,

                            debtor.participantName,

                            creditor.participantId,

                            creditor.participantName,

                            toEuros(
                                    transferCents
                            )
                    )
            );


            /*
             * Aggiorniamo i saldi temporanei.
             */
            debtor.balanceCents +=
                    transferCents;


            creditor.balanceCents -=
                    transferCents;


            /*
             * Se il debitore ha finito di pagare,
             * passiamo al prossimo.
             */
            if (debtor.balanceCents == 0) {

                debtorIndex++;
            }


            /*
             * Se il creditore ha ricevuto tutto,
             * passiamo al prossimo.
             */
            if (creditor.balanceCents == 0) {

                creditorIndex++;
            }
        }


        return settlements;
    }


    /*
     * Converte un Double in centesimi.
     *
     * 35.50 € -> 3550
     */
    private long toCents(
            Double amount) {


        if (amount == null) {

            return 0;
        }


        return Math.round(
                amount * 100.0
        );
    }


    /*
     * Converte i centesimi in euro.
     *
     * 3550 -> 35.50 €
     */
    private double toEuros(
            long cents) {


        return cents / 100.0;
    }


    /*
     * Classe interna utilizzata solamente
     * durante il calcolo dei debiti.
     */
    private static class BalanceHolder {


        private final Long participantId;

        private final String participantName;

        private long balanceCents;


        public BalanceHolder(
                Long participantId,
                String participantName,
                long balanceCents) {


            this.participantId =
                    participantId;


            this.participantName =
                    participantName;


            this.balanceCents =
                    balanceCents;
        }
    }
}