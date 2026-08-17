package com.example.trtform.views;

import com.example.trtform.service.ParticipationService;
import com.example.trtform.service.QuestionService;
import com.example.trtform.service.SurveyService;
import com.example.trtform.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.*;

@Route("survey-results")
@PageTitle("Anket Sonuçları | Dinamik Anket")
public class SurveyResultsView extends VerticalLayout implements BeforeEnterObserver, HasUrlParameter<Long> {

    private final UserService userService;
    private final QuestionService questionService;
    private final ParticipationService participationService;
    private final SurveyService surveyService;
    
    private Long surveyId;
    private VerticalLayout contentLayout;
    private Paragraph summaryParagraph;
    private ComboBox<String> globalChartTypeBox;

    public SurveyResultsView(UserService userService, QuestionService questionService, ParticipationService participationService, SurveyService surveyService) {
        this.userService = userService;
        this.questionService = questionService;
        this.participationService = participationService;
        this.surveyService = surveyService;

        // --- SAYFA ARKA PLANI VE GENEL AYARLAR ---
        setSizeFull();
        getStyle().set("background", "linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%)"); 
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);
        getStyle().set("overflow-y", "auto");
        setPadding(true);

        // --- ANA KART (İçerikleri saran şık beyaz kutu) ---
        VerticalLayout mainCard = new VerticalLayout();
        mainCard.setWidth("1000px");
        mainCard.setMaxWidth("100%");
        mainCard.getStyle().set("background", "white");
        mainCard.getStyle().set("border-radius", "16px");
        mainCard.getStyle().set("box-shadow", "0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)");
        mainCard.setPadding(true);
        mainCard.setSpacing(true);
        mainCard.getStyle().set("margin", "20px auto");

        // Başlık
        H2 title = new H2("📈 Anket Sonuç Raporu ve Grafikler");
        title.getStyle().set("margin", "0");
        title.getStyle().set("color", "var(--lumo-primary-text-color)");

        // Geri Dön Butonu
        Button backButton = new Button("Geri Dön", new Icon(VaadinIcon.ARROW_LEFT), event -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Header Alanı
        HorizontalLayout headerLayout = new HorizontalLayout(title, backButton);
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");
        headerLayout.getStyle().set("padding-bottom", "15px");

        summaryParagraph = new Paragraph("Yükleniyor...");
        summaryParagraph.getStyle().set("font-weight", "500").set("color", "var(--lumo-secondary-text-color)");

        // Grafik türü seçimi için ComboBox
        globalChartTypeBox = new ComboBox<>("Grafik Görünümü");
        globalChartTypeBox.setItems("Sütun Grafik", "Daire Grafik", "Tablo Görünümü");
        globalChartTypeBox.setValue("Sütun Grafik");
        globalChartTypeBox.setWidth("200px");
        globalChartTypeBox.addValueChangeListener(e -> loadResults());

        HorizontalLayout topControlLayout = new HorizontalLayout(globalChartTypeBox);
        topControlLayout.setWidthFull();
        topControlLayout.setJustifyContentMode(JustifyContentMode.END);

        contentLayout = new VerticalLayout();
        contentLayout.setWidthFull();
        contentLayout.setPadding(false);

        // Ana kartın içine elemanları ekle
        mainCard.add(headerLayout, summaryParagraph, topControlLayout, contentLayout);
        add(mainCard);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (userService.getLoggedInUser() == null) {
            Notification.show("Bu sayfaya erişmek için giriş yapmalısınız!", 3000, Notification.Position.MIDDLE);
            event.forwardTo("login");
        }
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        this.surveyId = parameter;
        loadResults();
    }

    private void loadResults() {
        if (surveyId == null) return;

        List<MainView.SurveyDto> surveys = surveyService.getSurveys();
        String surveyName = "Anket";
        String surveyDesc = "";
        for (MainView.SurveyDto s : surveys) {
            if (s.getId().equals(surveyId)) {
                surveyName = s.getName();
                surveyDesc = s.getDescription();
                break;
            }
        }

        List<QuestionService.QuestionDto> questions = questionService.getQuestionsBySurveyId(surveyId);
        List<ParticipationService.AnswerDto> answers = participationService.getAnswersBySurveyId(surveyId);

        Set<String> uniqueParticipants = new HashSet<>();
        for (ParticipationService.AnswerDto ans : answers) {
            if (ans.getParticipantName() != null) {
                uniqueParticipants.add(ans.getParticipantName());
            }
        }

        summaryParagraph.setText("📌 Anket Adı: " + surveyName + " | Açıklama: " + surveyDesc + " | Toplam Katılımcı: " + uniqueParticipants.size());
        contentLayout.removeAll();

        VerticalLayout overviewLayout = new VerticalLayout();
        overviewLayout.setPadding(false);
        overviewLayout.setSpacing(true);
        overviewLayout.setWidthFull();
        overviewLayout.getStyle().set("margin-top", "4px");

        HorizontalLayout summaryCards = new HorizontalLayout();
        summaryCards.setWidthFull();
        summaryCards.setSpacing(true);
        summaryCards.setPadding(false);
        summaryCards.add(
                createSummaryCard("Toplam Soru", String.valueOf(questions.size())),
                createSummaryCard("Toplam Katılımcı", String.valueOf(uniqueParticipants.size())),
                createSummaryCard("Toplam Yanıt", String.valueOf(answers.size()))
        );
        overviewLayout.add(summaryCards);
        contentLayout.add(overviewLayout);

        if (questions.isEmpty()) {
            contentLayout.add(new Paragraph("Bu ankete henüz soru eklenmemiş."));
            return;
        }

        String selectedView = globalChartTypeBox.getValue();

        for (int i = 0; i < questions.size(); i++) {
            QuestionService.QuestionDto q = questions.get(i);
            
            VerticalLayout questionBox = new VerticalLayout();
            questionBox.setWidthFull();
            questionBox.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
            questionBox.getStyle().set("border-radius", "12px");
            questionBox.getStyle().set("padding", "16px");
            questionBox.getStyle().set("margin-bottom", "16px");
            questionBox.getStyle().set("background", "linear-gradient(180deg, var(--lumo-base-color) 0%, var(--lumo-contrast-5pct) 100%)");
            questionBox.getStyle().set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.06)");

            H4 qTitle = new H4((i + 1) + ". Soru: " + q.getText());
            qTitle.getStyle().set("margin", "0");
            Paragraph qTypeInfo = new Paragraph("Soru Türü: " + q.getType());
            qTypeInfo.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)").set("margin", "0 0 10px 0");

            List<ParticipationService.AnswerDto> qAnswers = new ArrayList<>();
            for (ParticipationService.AnswerDto ans : answers) {
                if (ans.getQuestionId().equals(q.getId())) {
                    qAnswers.add(ans);
                }
            }

            HorizontalLayout questionHeader = new HorizontalLayout();
            questionHeader.setWidthFull();
            questionHeader.setJustifyContentMode(JustifyContentMode.BETWEEN);
            questionHeader.setAlignItems(Alignment.CENTER);

            Span answerCountBadge = new Span(qAnswers.size() + " yanıt");
            answerCountBadge.getStyle().set("background", "var(--lumo-primary-color-10pct)")
                    .set("color", "var(--lumo-primary-text-color)")
                    .set("padding", "4px 10px")
                    .set("border-radius", "999px")
                    .set("font-size", "var(--lumo-font-size-s)");

            questionHeader.add(qTitle, answerCountBadge);
            questionBox.add(questionHeader, qTypeInfo);

            if (qAnswers.isEmpty()) {
                questionBox.add(new Paragraph("Bu soruya henüz yanıt verilmemiş."));
            } else {
                boolean isChoiceBased = "Çoktan Seçmeli".equals(q.getType()) 
                        || "Onay Kutuları".equals(q.getType()) 
                        || "Açılır Menü".equals(q.getType());

                if (isChoiceBased) {
                    Map<String, Integer> optionCounts = new HashMap<>();
                    int totalSelectionCount = qAnswers.size();

                    for (ParticipationService.AnswerDto ans : qAnswers) {
                        String choice = ans.getAnswerText();
                        if (choice != null && !choice.isBlank()) {
                            optionCounts.put(choice, optionCounts.getOrDefault(choice, 0) + 1);
                        }
                    }

                    if ("Sütun Grafik".equals(selectedView)) {
                        VerticalLayout barChartLayout = new VerticalLayout();
                        barChartLayout.setPadding(false);
                        barChartLayout.setSpacing(true);
                        barChartLayout.setWidthFull();

                        Div chart = new Div();
                        chart.setWidthFull();
                        chart.setHeight("210px");
                        chart.getStyle().set("display", "flex");
                        chart.getStyle().set("align-items", "center");
                        chart.getStyle().set("justify-content", "center");
                        chart.getStyle().set("margin-top", "8px");
                        chart.getStyle().set("max-width", "100%");
                        chart.getStyle().set("overflow-x", "auto");
                        chart.getStyle().set("overflow-y", "hidden");
                        chart.getElement().setProperty("innerHTML", buildBarChartSvg(optionCounts, totalSelectionCount));

                        VerticalLayout legend = new VerticalLayout();
                        legend.setPadding(false);
                        legend.setSpacing(false);
                        legend.setWidthFull();

                        List<Map.Entry<String, Integer>> entries = new ArrayList<>(optionCounts.entrySet());
                        for (int index = 0; index < entries.size(); index++) {
                            Map.Entry<String, Integer> entry = entries.get(index);
                            double percentage = totalSelectionCount > 0 ? (entry.getValue() * 100.0) / totalSelectionCount : 0.0;

                            HorizontalLayout legendRow = new HorizontalLayout();
                            legendRow.setAlignItems(Alignment.CENTER);
                            legendRow.setSpacing(true);

                            Div colorBox = new Div();
                            colorBox.setWidth("12px");
                            colorBox.setHeight("12px");
                            colorBox.getStyle().set("background", getPieChartColor(index));
                            colorBox.getStyle().set("border-radius", "3px");

                            Span legendText = new Span(entry.getKey() + " — " + entry.getValue() + " oy (" + String.format(Locale.US, "%.1f", percentage) + "%)");
                            legendText.getStyle().set("font-size", "var(--lumo-font-size-s)");

                            legendRow.add(colorBox, legendText);
                            legend.add(legendRow);
                        }

                        barChartLayout.add(chart, legend);
                        questionBox.add(barChartLayout);

                    } else if ("Daire Grafik".equals(selectedView)) {
                        VerticalLayout pieChartLayout = new VerticalLayout();
                        pieChartLayout.setPadding(false);
                        pieChartLayout.setSpacing(true);
                        pieChartLayout.setWidthFull();

                        Div chart = new Div();
                        chart.setWidth("180px");
                        chart.setHeight("180px");
                        chart.getStyle().set("display", "flex");
                        chart.getStyle().set("align-items", "center");
                        chart.getStyle().set("justify-content", "center");
                        chart.getElement().setProperty("innerHTML", buildPieChartSvg(optionCounts, totalSelectionCount));

                        VerticalLayout legend = new VerticalLayout();
                        legend.setPadding(false);
                        legend.setSpacing(false);
                        legend.setWidthFull();

                        List<Map.Entry<String, Integer>> entries = new ArrayList<>(optionCounts.entrySet());
                        for (int index = 0; index < entries.size(); index++) {
                            Map.Entry<String, Integer> entry = entries.get(index);
                            double percentage = totalSelectionCount > 0 ? (entry.getValue() * 100.0) / totalSelectionCount : 0.0;

                            HorizontalLayout legendRow = new HorizontalLayout();
                            legendRow.setAlignItems(Alignment.CENTER);
                            legendRow.setSpacing(true);

                            Div colorBox = new Div();
                            colorBox.setWidth("12px");
                            colorBox.setHeight("12px");
                            colorBox.getStyle().set("background", getPieChartColor(index));
                            colorBox.getStyle().set("border-radius", "3px");

                            Span legendText = new Span(entry.getKey() + " — " + entry.getValue() + " oy (" + String.format(Locale.US, "%.1f", percentage) + "%)");
                            legendText.getStyle().set("font-size", "var(--lumo-font-size-s)");

                            legendRow.add(colorBox, legendText);
                            legend.add(legendRow);
                        }

                        HorizontalLayout pieContainer = new HorizontalLayout(chart, legend);
                        pieContainer.setWidthFull();
                        pieContainer.setSpacing(true);
                        pieContainer.setAlignItems(Alignment.CENTER);
                        pieChartLayout.add(pieContainer);
                        questionBox.add(pieChartLayout);

                    } else {
                        // Tablo Görünümü
                        List<OptionStatDto> statsList = new ArrayList<>();
                        for (Map.Entry<String, Integer> entry : optionCounts.entrySet()) {
                            double percentage = totalSelectionCount > 0 ? (entry.getValue() * 100.0) / totalSelectionCount : 0.0;
                            statsList.add(new OptionStatDto(entry.getKey(), entry.getValue(), String.format(Locale.US, "%.1f", percentage) + "%"));
                        }

                        Grid<OptionStatDto> statGrid = new Grid<>(OptionStatDto.class);
                        statGrid.removeAllColumns();
                        
                        // Tabloya yeni çizgili tasarım eklendi
                        statGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

                        statGrid.addColumn(option -> option.getOptionText()).setHeader("Seçenek").setAutoWidth(true);
                        statGrid.addColumn(option -> option.getCount()).setHeader("Oy Sayısı").setAutoWidth(true);
                        statGrid.addColumn(option -> option.getPercentage()).setHeader("Yüzde Oranı").setAutoWidth(true);
                        statGrid.setItems(statsList);
                        statGrid.setAllRowsVisible(true);
                        statGrid.setWidthFull();

                        questionBox.add(statGrid);
                    }

                } else {
                    // Metin tabanlı yanıtlar
                    VerticalLayout answersListLayout = new VerticalLayout();
                    answersListLayout.setPadding(false);
                    answersListLayout.setSpacing(false);

                    for (ParticipationService.AnswerDto ans : qAnswers) {
                        String pName = ans.getParticipantName() != null ? ans.getParticipantName() : "Anonim";
                        String text = ans.getAnswerText() != null ? ans.getAnswerText() : "";
                        Paragraph p = new Paragraph("• " + pName + ": " + text);
                        p.getStyle().set("margin", "4px 0").set("font-size", "var(--lumo-font-size-s)");
                        answersListLayout.add(p);
                    }
                    questionBox.add(answersListLayout);
                }

                // Senin Eklediğin Özel Alan: Kimler ne yanıt verdi?
                VerticalLayout respondentsLayout = createRespondentBreakdown(qAnswers);
                if (respondentsLayout != null) {
                    questionBox.add(respondentsLayout);
                }
            }

            contentLayout.add(questionBox);
        }
    }

    private VerticalLayout createRespondentBreakdown(List<ParticipationService.AnswerDto> qAnswers) {
        if (qAnswers == null || qAnswers.isEmpty()) {
            return null;
        }

        VerticalLayout respondentsLayout = new VerticalLayout();
        respondentsLayout.setPadding(false);
        respondentsLayout.setSpacing(false);
        respondentsLayout.setWidthFull();
        respondentsLayout.getStyle().set("margin-top", "12px");
        respondentsLayout.getStyle().set("border-top", "1px dashed var(--lumo-contrast-20pct)");
        respondentsLayout.getStyle().set("padding-top", "12px");

        H5 header = new H5("👥 Kimler ne yanıt verdi?");
        header.getStyle().set("margin", "0 0 8px 0");
        respondentsLayout.add(header);

        for (ParticipationService.AnswerDto ans : qAnswers) {
            String participantName = ans.getParticipantName() != null ? ans.getParticipantName() : "Anonim";
            String answerText = ans.getAnswerText() != null ? ans.getAnswerText() : "-";

            Span row = new Span(participantName + " ➔ " + answerText);
            row.getStyle().set("font-size", "var(--lumo-font-size-s)")
                    .set("display", "block")
                    .set("margin-bottom", "4px")
                    .set("color", "var(--lumo-secondary-text-color)");
            respondentsLayout.add(row);
        }

        return respondentsLayout;
    }

    private Div createSummaryCard(String title, String value) {
        Div card = new Div();
        card.getStyle().set("background", "linear-gradient(135deg, var(--lumo-primary-color-10pct), var(--lumo-base-color))");
        card.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        card.getStyle().set("border-radius", "10px");
        card.getStyle().set("padding", "14px 16px");
        card.getStyle().set("flex", "1");
        card.getStyle().set("min-width", "180px");

        H5 cardTitle = new H5(title);
        cardTitle.getStyle().set("margin", "0 0 6px 0").set("font-size", "var(--lumo-font-size-s)");

        Span cardValue = new Span(value);
        cardValue.getStyle().set("font-size", "var(--lumo-font-size-xl)").set("font-weight", "700");

        card.add(cardTitle, cardValue);
        return card;
    }

    private String buildBarChartSvg(Map<String, Integer> optionCounts, int totalSelectionCount) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(optionCounts.entrySet());
        if (entries.isEmpty()) {
            return "<svg viewBox='0 0 560 210' width='100%' height='210'></svg>";
        }

        int chartWidth = 560;
        int chartHeight = 210;
        int marginLeft = 60;
        int marginTop = 20;
        int marginRight = 20;
        int marginBottom = 40;
        int plotWidth = chartWidth - marginLeft - marginRight;
        int plotHeight = chartHeight - marginTop - marginBottom;
        int maxValue = entries.stream().mapToInt(entry -> entry.getValue()).max().orElse(1);

        StringBuilder svg = new StringBuilder();
        svg.append("<svg viewBox='0 0 ").append(chartWidth).append(" ").append(chartHeight).append("' width='100%' height='210' preserveAspectRatio='xMidYMid meet' role='img' aria-label='Sütun grafiği'>");
        svg.append("<rect x='0' y='0' width='").append(chartWidth).append("' height='").append(chartHeight).append("' rx='12' fill='white'></rect>");
        svg.append("<line x1='").append(marginLeft).append("' y1='").append(chartHeight - marginBottom).append("' x2='").append(chartWidth - marginRight).append("' y2='").append(chartHeight - marginBottom).append("' stroke='var(--lumo-contrast-20pct)' stroke-width='1'></line>");
        svg.append("<line x1='").append(marginLeft).append("' y1='").append(marginTop).append("' x2='").append(marginLeft).append("' y2='").append(chartHeight - marginBottom).append("' stroke='var(--lumo-contrast-20pct)' stroke-width='1'></line>");

        double barWidth = Math.max(20, plotWidth / Math.max(1, entries.size()) * 0.5);
        double gap = plotWidth / Math.max(1, entries.size()) * 0.5;

        for (int index = 0; index < entries.size(); index++) {
            Map.Entry<String, Integer> entry = entries.get(index);
            int value = entry.getValue();
            double ratio = maxValue > 0 ? value / (double) maxValue : 0;
            double barHeight = ratio * plotHeight;
            double x = marginLeft + index * (barWidth + gap) + 10;
            double y = chartHeight - marginBottom - barHeight;

            svg.append("<rect x='").append(String.format(Locale.US, "%.1f", x)).append("' y='").append(String.format(Locale.US, "%.1f", y)).append("' width='").append(String.format(Locale.US, "%.1f", barWidth)).append("' height='").append(String.format(Locale.US, "%.1f", barHeight)).append("' rx='6' fill='").append(getPieChartColor(index)).append("'></rect>");
            svg.append("<text x='").append(String.format(Locale.US, "%.1f", x + barWidth / 2)).append("' y='").append(chartHeight - marginBottom + 18).append("' text-anchor='middle' font-size='10' fill='var(--lumo-secondary-text-color)'>Seçenek ").append(index + 1).append("</text>");
            svg.append("<text x='").append(String.format(Locale.US, "%.1f", x + barWidth / 2)).append("' y='").append(String.format(Locale.US, "%.1f", y - 6)).append("' text-anchor='middle' font-size='11' font-weight='700' fill='var(--lumo-primary-text-color)'>").append(value).append("</text>");
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private String buildPieChartSvg(Map<String, Integer> optionCounts, int totalSelectionCount) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(optionCounts.entrySet());
        if (entries.isEmpty()) {
            return "<svg viewBox='0 0 180 180' width='180' height='180'></svg>";
        }

        double startAngle = -90.0;
        StringBuilder svg = new StringBuilder();
        svg.append("<svg viewBox='0 0 180 180' width='180' height='180' role='img' aria-label='Seçenek dağılımı'>");
        svg.append("<circle cx='90' cy='90' r='75' fill='none' stroke='var(--lumo-contrast-10pct)' stroke-width='30'></circle>");

        for (int index = 0; index < entries.size(); index++) {
            Map.Entry<String, Integer> entry = entries.get(index);
            double percentage = totalSelectionCount > 0 ? (entry.getValue() * 100.0) / totalSelectionCount : 0.0;
            double sliceAngle = percentage / 100.0 * 360.0;
            if (sliceAngle <= 0) {
                continue;
            }

            double endAngle = startAngle + sliceAngle;
            String path = describePieSlice(90, 90, 75, startAngle, endAngle);
            svg.append("<path d='").append(path).append("' fill='").append(getPieChartColor(index)).append("'></path>");
            startAngle = endAngle;
        }

        svg.append("<circle cx='90' cy='90' r='45' fill='white'></circle>");
        svg.append("</svg>");
        return svg.toString();
    }

    private String describePieSlice(int centerX, int centerY, int radius, double startAngle, double endAngle) {
        double startRad = Math.toRadians(startAngle);
        double endRad = Math.toRadians(endAngle);

        double x1 = centerX + radius * Math.cos(startRad);
        double y1 = centerY + radius * Math.sin(startRad);
        double x2 = centerX + radius * Math.cos(endRad);
        double y2 = centerY + radius * Math.sin(endRad);
        int largeArcFlag = (endAngle - startAngle) > 180 ? 1 : 0;

        return "M " + centerX + " " + centerY
                + " L " + x1 + " " + y1
                + " A " + radius + " " + radius + " 0 " + largeArcFlag + " 1 " + x2 + " " + y2
                + " Z";
    }

    private String getPieChartColor(int index) {
        String[] palette = {"#2563eb", "#06b6d4", "#f59e0b", "#ec4899", "#10b981", "#8b5cf6"};
        return palette[index % palette.length];
    }

    public static class OptionStatDto {
        private String optionText;
        private int count;
        private String percentage;

        public OptionStatDto(String optionText, int count, String percentage) {
            this.optionText = optionText;
            this.count = count;
            this.percentage = percentage;
        }

        public String getOptionText() { return optionText; }
        public int getCount() { return count; }
        public String getPercentage() { return percentage; }
    }
}