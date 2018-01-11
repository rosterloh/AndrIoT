package com.rosterloh.andriot.ui.dash;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.rosterloh.andriot.R;
import com.rosterloh.andriot.databinding.GraphFragmentBinding;
import com.rosterloh.andriot.db.SensorData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class GraphFragment extends DaggerFragment {

    @Inject
    DashViewModelFactory mViewModelFactory;

    private GraphFragmentBinding mBinding;
    private DashViewModel mDashViewModel;

    private LineChart mChart;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.graph_fragment, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDashViewModel = ViewModelProviders.of(this, mViewModelFactory).get(DashViewModel.class);

        initGraphView();

        mDashViewModel.getSensorDataList().observe(this, data -> {
            if (data != null) {
                drawGraph(data);
                if (data.size() > 0) {
                    mBinding.setSensors(data.get(data.size() - 1));
                }
            }
        });
    }

    private void initGraphView() {
        mChart = mBinding.graph;
        mChart.getDescription().setEnabled(false);

        int textColour = ContextCompat.getColor(getContext(), R.color.textColorHint);
        LineData lineData = new LineData();
        lineData.setValueTextColor(textColour);
        mChart.setData(lineData);
        mChart.invalidate();

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(textColour);
        l.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setTextColor(textColour);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f); // one hour
        xAxis.setValueFormatter((value, axis) -> {
            LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond((long) value), ZoneId.systemDefault());
            return dt.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"));
        });

        YAxis leftAxis = mChart.getAxisLeft();
        //leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(textColour);
        //leftAxis.setAxisMaximum(1500f);
        //leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = mChart.getAxisRight();
        //rightAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        rightAxis.setTextColor(textColour);
        //rightAxis.setAxisMaximum(200f);
        //rightAxis.setAxisMinimum(0f);
        rightAxis.setDrawGridLines(false);
    }

    private void drawGraph(List<SensorData> data) {

        ArrayList<Entry> co2Values = new ArrayList<Entry>();
        ArrayList<Entry> tvocValues = new ArrayList<Entry>();
        ArrayList<Entry> temperatureValues = new ArrayList<Entry>();
        ArrayList<Entry> humidityValues = new ArrayList<Entry>();

        for (SensorData d : data) {

            long millis = d.getTime().atZone(ZoneId.systemDefault()).toEpochSecond();

            co2Values.add(new Entry(millis, d.getECO2()));
            tvocValues.add(new Entry(millis, d.getTVOC()));
            temperatureValues.add(new Entry(millis, d.getTemperature()));
            humidityValues.add(new Entry(millis, d.getHumidity()));
        }

        LineDataSet co2Set = new LineDataSet(co2Values, "eCO2");
        co2Set.setAxisDependency(YAxis.AxisDependency.LEFT);
        co2Set.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        //co2Set.setValueTextColor(ColorTemplate.getHoloBlue());
        co2Set.setLineWidth(1.5f);
        co2Set.setDrawCircles(false);
        co2Set.setDrawValues(false);
        co2Set.setFillColor(ColorTemplate.MATERIAL_COLORS[0]);
        co2Set.setHighLightColor(Color.rgb(244, 117, 117));
        co2Set.setDrawCircleHole(false);

        LineDataSet tvocSet = new LineDataSet(tvocValues, "TVOC");
        tvocSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        tvocSet.setColor(ColorTemplate.MATERIAL_COLORS[1]);
        //tvocSet.setValueTextColor(ColorTemplate.getHoloBlue());
        tvocSet.setLineWidth(1.5f);
        tvocSet.setDrawCircles(false);
        tvocSet.setDrawValues(false);
        tvocSet.setFillColor(ColorTemplate.MATERIAL_COLORS[1]);
        tvocSet.setHighLightColor(Color.rgb(244, 117, 117));
        tvocSet.setDrawCircleHole(false);

        LineDataSet temperatureSet = new LineDataSet(temperatureValues, "Temperature");
        temperatureSet.setColor(ColorTemplate.MATERIAL_COLORS[2]);
        temperatureSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        temperatureSet.setDrawCircles(false);

        LineDataSet humiditySet = new LineDataSet(humidityValues, "Humidity");
        humiditySet.setColor(ColorTemplate.MATERIAL_COLORS[3]);
        humiditySet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        humiditySet.setDrawCircles(false);

        LineData d = new LineData();
        d.addDataSet(co2Set);
        d.addDataSet(tvocSet);
        d.addDataSet(temperatureSet);
        d.addDataSet(humiditySet);

        mChart.setData(d);
        mChart.invalidate();
        mChart.animateX(2000);
    }
}
