package it.uniroma3.siw.travelly.dto;

import java.util.List;

public record ExpenseDashboardResponse(

        double totalExpenses,

        double unassignedExpenses,

        boolean settlementAvailable,

        String settlementMessage,

        List<CategoryTotal> categories,

        List<ParticipantBalance> participants,

        List<Settlement> settlements) {


    /*
     * Totale delle spese per categoria.
     *
     * Esempio:
     *
     * ACCOMMODATION -> 550 €
     * CULTURE       -> 100 €
     */
    public record CategoryTotal(

            String category,

            double amount) {
    }


    /*
     * Situazione economica di ogni partecipante.
     *
     * paidAmount:
     * quanto ha effettivamente pagato
     *
     * fairShare:
     * quanto dovrebbe pagare dividendo
     * equamente tutte le spese
     *
     * balance:
     * paidAmount - fairShare
     */
    public record ParticipantBalance(

            Long participantId,

            String participantName,

            double paidAmount,

            Double fairShare,

            Double balance,

            String status) {
    }


    /*
     * Trasferimento necessario
     * per pareggiare i conti.
     *
     * Esempio:
     *
     * Gabri -> Alessia -> 200 €
     */
    public record Settlement(

            Long fromParticipantId,

            String fromParticipantName,

            Long toParticipantId,

            String toParticipantName,

            double amount) {
    }
}