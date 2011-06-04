class Altersegtomeasure < ActiveRecord::Migration
  def self.up
    rename_column :measures, :urban_id ,:segment_id
  end

  def self.down
  end
end
